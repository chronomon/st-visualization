package com.jd.st.data.spark

import com.jd.st.data.storage.hbase.HBaseAdaptor
import com.jd.st.data.storage.model._
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hbase.{HBaseConfiguration, KeyValue, TableName}
import org.apache.hadoop.hbase.client.ConnectionFactory
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{HFileOutputFormat2, TableOutputFormat}
import org.apache.hadoop.hbase.tool.LoadIncrementalHFiles
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._

/**
 * 导入Taxi数据到HBase表
 */
object TaxiImportApp {

  def main(args: Array[String]): Unit = {
    val maxLevel = 18 //args(0).toShort
    val extent = 512 //args(1).toInt
    val hours = 1 //args(2).toInt // hours
    val tablePrefix = s"tile:experiment_${maxLevel}_${extent}_$hours"

    val period = new Period(hours)
    val pyramid = new BinTilePyramid(period, maxLevel, extent)

    val sparkConf = new SparkConf()
      .setAppName("VectorTileApp")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    //.setMaster("local[*]")
    val sc = new SparkContext(sparkConf)

    //HBaseAdaptor.deleteTable(tablePrefix + "_packet")
    //HBaseAdaptor.deleteTable(tablePrefix + "_count")

    //HBaseAdaptor.createTable(tablePrefix + "_packet")
    //HBaseAdaptor.createTable(tablePrefix + "_count")

    dataImport(sc, tablePrefix, pyramid, args(0))

    sc.stop()
  }

  private def xianData(sc: SparkContext, fileName: String): RDD[GeodeticFeature] = {
    val path = "hdfs://11.51.204.127:8020/wangrubin/" + fileName + ".csv"
    sc.textFile(path, 150)
      .zipWithIndex()
      .mapPartitions(iter => {
        iter.flatMap(idAndLine => {
          val id = idAndLine._2.toString
          val attrs = idAndLine._1.split(",\"")
          val gpsRecords = attrs(1).drop(1).dropRight(2).split(",")
          gpsRecords.map(record => {
            val values = record.trim.split(" ")
            val x = values(0).toDouble
            val y = values(1).toDouble
            val epochSeconds = values(2).toLong
            new GeodeticFeature(id, x, y, epochSeconds)
          }).toIterator
        })
      })
  }

  private def dataImport(sc: SparkContext,
                         tablePrefix: String,
                         pyramid: BinTilePyramid,
                         fileName: String): Unit = {

    val featureRDD: RDD[GeodeticFeature] = xianData(sc, fileName)
    val bcPyramid = sc.broadcast(pyramid)

    // transform
    val groupRDD = featureRDD.mapPartitions(iter => {
      iter.map(feature => {
        val pair = bcPyramid.value.locateFeature(feature)
        (pair.getKey, pair.getValue)
      })
    }).groupBy(_._1)

    // 存储位置数据包
    val packetRDD = groupRDD.mapPartitions(iter => {
      iter.map(tuple => {
        val binTileFeature = tuple._1
        val features = tuple._2.map(_._2).toList.asJava
        binTileFeature.setFeatures(features)
        binTileFeature.encodeKV()
      })
    })
    saveHBase(tablePrefix + "_packet", packetRDD)

    // 存储统计量数据
    val countRDD = groupRDD.mapPartitions(iter => {
      iter.map(tuple => {
        val binTileFeature = tuple._1
        val count = tuple._2.map(_._2).size
        (binTileFeature.binEpochSeconds, (binTileFeature.z2, count.toLong))
      })
    }).groupBy(_._1)
      .mapPartitions(iter => {
        iter.map(tuple => {
          val binEpochSeconds = tuple._1
          val z2CountMap = tuple._2.map(_._2)
            .groupBy(_._1).mapValues(iter => iter.map(_._2).sum)
            .map(kv => (kv._1.asInstanceOf[java.lang.Long], kv._2.asInstanceOf[java.lang.Long]))
            .asJava

          val binTileCount = new BinCount(binEpochSeconds)
          binTileCount.setZ2Count(z2CountMap)
          binTileCount.encodeKV()
        })
      })
    saveHBase(tablePrefix + "_count", countRDD)
  }

  private def saveHBase(tableNameStr: String, rdd: RDD[KVPair]): Unit = {
    val hbaseConf = HBaseConfiguration.create(rdd.sparkContext.hadoopConfiguration)
    //IMPORTANT: must set the attribute to solve the problem (can't create path from null string )
    //hbaseConf.set("mapreduce.output.fileoutputformat.outputdir", "/tmp")
    hbaseConf.set(TableOutputFormat.OUTPUT_TABLE, tableNameStr)
    hbaseConf.set("zookeeper.znode.parent", "/hbase-unsecure")
    hbaseConf.setInt("hbase.mapreduce.bulkload.max.hfiles.perRegion.perFamily", 4096)


    val kvRDD = rdd
      .map(kvPair => {
        val kv = new KeyValue(kvPair.key, HBaseAdaptor.CF, HBaseAdaptor.CQ, kvPair.value)
        (new ImmutableBytesWritable(kvPair.key), kv)
      }).sortBy(_._1)

    val connection = ConnectionFactory.createConnection(hbaseConf)
    val tableName = TableName.valueOf(tableNameStr)
    val table = connection.getTable(tableName)

    try {
      val regionLocator = connection.getRegionLocator(tableName)
      val job = Job.getInstance(hbaseConf)
      job.setMapOutputKeyClass(classOf[ImmutableBytesWritable])
      job.setMapOutputValueClass(classOf[KeyValue])
      val savePath = "hdfs://11.51.204.127:8020/wangrubin/tmp"
      delHdfsPath(savePath, rdd.sparkContext)
      job.getConfiguration.set("mapred.output.dir", savePath)

      HFileOutputFormat2.configureIncrementalLoad(job, table, regionLocator)
      kvRDD.saveAsNewAPIHadoopDataset(job.getConfiguration)

      val bulkLoader = new LoadIncrementalHFiles(hbaseConf)
      bulkLoader.doBulkLoad(new Path(savePath), connection.getAdmin, table, regionLocator)

    } finally {
      table.close()
      connection.close()
    }
  }

  private def delHdfsPath(path: String, sc: SparkContext) {
    val hdfs = FileSystem.get(sc.hadoopConfiguration)
    val hdfsPath = new Path(path)
    if (hdfs.exists(hdfsPath)) {
      hdfs.delete(hdfsPath, true)
    }
  }
}

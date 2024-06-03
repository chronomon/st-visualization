package com.jd.st.data.spark

import com.jd.st.data.storage.hbase.HBaseAdaptor
import com.jd.st.data.storage.model._
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._

/**
 * 导入Taxi数据到HBase表
 */
object TaxiSparkApp {

  def main(args: Array[String]): Unit = {
    val maxLevel = args(0).toShort
    val extent = args(1).toInt
    val hours = args(2).toInt // hours
    val tablePrefix = s"tile:experiment_${maxLevel}_${extent}_$hours"

    val period = new Period(hours)
    val pyramid = new BinTilePyramid(period, maxLevel, extent)

    val sparkConf = new SparkConf()
      .setAppName("VectorTileApp")
    //.setMaster("local[*]")
    val sc = new SparkContext(sparkConf)

    HBaseAdaptor.deleteTable(tablePrefix + "_raw")
    HBaseAdaptor.deleteTable(tablePrefix + "_pixel")
    HBaseAdaptor.deleteTable(tablePrefix + "_packet")
    HBaseAdaptor.deleteTable(tablePrefix + "_count")

    HBaseAdaptor.createTable(tablePrefix + "_raw")
    HBaseAdaptor.createTable(tablePrefix + "_pixel")
    HBaseAdaptor.createTable(tablePrefix + "_packet")
    HBaseAdaptor.createTable(tablePrefix + "_count")

    dataImport(sc, tablePrefix, pyramid)

    sc.stop()
  }

  private def xianData(sc: SparkContext): RDD[GeodeticFeature] = {
    val path = "hdfs://11.51.204.127:8020/wangrubin/xian_traj_1015.csv"
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
                         pyramid: BinTilePyramid): Unit = {

    val featureRDD: RDD[GeodeticFeature] = xianData(sc)
    val bcPyramid = sc.broadcast(pyramid)

    // 存储原始位置数据
    val rawRDD = featureRDD.mapPartitions(iter => {
      iter.map(feature => {
        val pair = bcPyramid.value.locateFeature(feature)
        feature.encode(pair.getKey)
      })
    })
    saveHBase(tablePrefix + "_raw", rawRDD)


    // 存储瓦片位置数据
    val pixelRDD = featureRDD.mapPartitions(iter => {
      iter.map(feature => {
        val pair = bcPyramid.value.locateFeature(feature)
        pair.getValue.encode(pair.getKey)
      })
    })
    saveHBase(tablePrefix + "_pixel", pixelRDD)


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

  private def saveHBase(tableName: String, rdd: RDD[KVPair]): Unit = {
    val conf = HBaseConfiguration.create
    //IMPORTANT: must set the attribute to solve the problem (can't create path from null string )
    //hbaseConf.set("mapreduce.output.fileoutputformat.outputdir", "/tmp")
    conf.set(TableOutputFormat.OUTPUT_TABLE, tableName)
    conf.set("zookeeper.znode.parent", "/hbase-unsecure")

    val job = Job.getInstance(conf)
    job.setOutputFormatClass(classOf[TableOutputFormat[ImmutableBytesWritable]])
    job.setOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setOutputValueClass(classOf[Put])

    rdd.map(kvPair => {
      val put = new Put(kvPair.key)
      put.addImmutable(HBaseAdaptor.CF, HBaseAdaptor.CQ, kvPair.value)
      (new ImmutableBytesWritable(), put)
    }).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }
}

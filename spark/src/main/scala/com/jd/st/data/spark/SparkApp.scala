package com.jd.st.data.spark

import com.jd.st.data.storage.hbase.HBaseAdaptor
import com.jd.st.data.storage.model.{BinCount, GeodeticFeature, KVPair}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.locationtech.jts.io.WKTReader

import java.util
import scala.collection.JavaConverters._

object SparkApp {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("VectorTileApp").setMaster("local[*]")
    val sc = new SparkContext(sparkConf)

    HBaseAdaptor.deleteTable(HBaseAdaptor.dataTableName)
    HBaseAdaptor.deleteTable(HBaseAdaptor.countTableName)
    HBaseAdaptor.createTable(HBaseAdaptor.dataTableName)
    HBaseAdaptor.createTable(HBaseAdaptor.countTableName)

    dataImport(sc)

    sc.stop()
  }

  private def xianData(sc: SparkContext): RDD[GeodeticFeature] = {
    val path = "hdfs://11.51.204.127:8020/wangrubin/xian_traj_sample.csv"
    sc.textFile(path)
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

  private def lorryData(sc: SparkContext): RDD[GeodeticFeature] = {
    val path = "D:\\JDNetDiskDownload\\clean\\lorry-clean-20140330\\lorry-sample.csv"
    sc.textFile(path)
      .zipWithIndex()
      .mapPartitions(iter => {
        val reader = new WKTReader()
        iter.flatMap(idAndLine => {
          val id = idAndLine._2.toString
          val attrs = idAndLine._1.split("\":\"")
          val lineStringWKT = attrs(2).dropRight(2)
          val lineString = reader.read(lineStringWKT)
          lineString.getCoordinates.map(coord => {
            val x = coord.getX
            val y = coord.getY
            val epochSeconds = coord.getM.toLong / 1000
            new GeodeticFeature(id, x, y, epochSeconds)
          }).toIterator
        })
      })
  }

  private def dataImport(sc: SparkContext): Unit = {

    val featureRDD: RDD[GeodeticFeature] = xianData(sc)

    // transform
    featureRDD.mapPartitions(iter => {
      iter.map(feature => {
        val pair = HBaseAdaptor.pyramid.locateFeature(feature)
        (pair.getKey, pair.getValue)
      })
    }).groupBy(_._1).mapPartitions(iter => {
      val pairs = new util.ArrayList[KVPair]()
      val list = iter.toList.map(tuple => {
        val binTileFeature = tuple._1
        val features = tuple._2.map(_._2).toList.asJava
        binTileFeature.setFeatures(features)
        val kvPair = binTileFeature.encodeKV()
        pairs.add(kvPair)
        println("瓦片包大小：" + kvPair.value.length + "字节")
        // 初始统计量
        (binTileFeature.binEpochSeconds, (binTileFeature.z2, features.size().toLong))
      })
      // 写入数据
      HBaseAdaptor.putData(HBaseAdaptor.dataTableName, pairs)
      list.toIterator
    }).groupBy(_._1).foreachPartition(iter => {
      val pairs = new util.ArrayList[KVPair]()
      iter.toList.foreach(tuple => {
        val binEpochSeconds = tuple._1
        val z2CountMap = tuple._2.map(_._2)
          .groupBy(_._1).mapValues(iter => iter.map(_._2).sum)
          .map(kv => (kv._1.asInstanceOf[java.lang.Long], kv._2.asInstanceOf[java.lang.Long]))
          .asJava

        val binTileCount = new BinCount(binEpochSeconds)
        binTileCount.setZ2Count(z2CountMap)
        val kvPair = binTileCount.encodeKV()
        pairs.add(kvPair)

        // 写入统计量
        println("统计量大小：" + (kvPair.value.length.toDouble / 1024).toInt + "KB")
      })
      HBaseAdaptor.putData(HBaseAdaptor.countTableName, pairs)
    })
  }
}

package com.naxions.flink.day06

import java.util

import com.naxions.flink.day02.{SensorReading, SensorSource}
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.apache.http.HttpHost
import org.elasticsearch.client.Requests

object WriteToEs {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val httpHost = new util.ArrayList[HttpHost]()
    httpHost.add(new HttpHost("db2", 9200, "http"))
    val esSinkBuilder = new ElasticsearchSink.Builder[SensorReading](
      httpHost,
      new ElasticsearchSinkFunction[SensorReading] {
        override def process(t: SensorReading, runtimeContext: RuntimeContext, requestIndexer: RequestIndexer): Unit = {
          val hashMap = new util.HashMap[String,String]()
          hashMap.put("data", t.toString)

          val indexRequest = Requests
            .indexRequest()
            .index("sensor")
            .`type`("readingData")
            .source(hashMap)

          requestIndexer.add(indexRequest)
        }
      }
    )

    esSinkBuilder.setBulkFlushMaxActions(1)
    val stream = env.addSource(new SensorSource)
    stream.addSink(esSinkBuilder.build())

    env.execute()

  }

}

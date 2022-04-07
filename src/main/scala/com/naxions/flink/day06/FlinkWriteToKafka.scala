package com.naxions.flink.day06

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011

object FlinkWriteToKafka {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.fromElements(
      "hello"
      ,"world"
    )

    stream.addSink(new FlinkKafkaProducer011[String](
      "db2:9092",
      "test2021",
      new SimpleStringSchema() // 使用字符串形式写入kafka
    ))

    env.execute()

  }

}

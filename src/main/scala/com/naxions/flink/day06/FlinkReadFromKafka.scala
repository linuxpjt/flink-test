package com.naxions.flink.day06

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

object FlinkReadFromKafka {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val props = new Properties()
    props.put("bootstrap.servers", "db2:9092")
    props.put("group.id","consumer-group")
    props.put("key.deserializer", "org.abootstrap.serverpache.kafka.common.serialization.StringDeSerializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeSerializer")
    props.put("auto.offset.reset", "latest")

    val stream = env.addSource(new FlinkKafkaConsumer011[String](
      "test2021",
      new SimpleStringSchema(),
      props
    ))
    stream.print()

    env.execute()

  }
}

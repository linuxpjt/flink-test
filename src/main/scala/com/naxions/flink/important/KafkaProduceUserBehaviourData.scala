package com.naxions.flink.important

import java.util.Properties

import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object KafkaProduceUserBehaviourData {
  def main(args: Array[String]): Unit = {
    writeToKafka("test2021")
  }

  def writeToKafka(topic: String): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "db2:9092")
    props.put(
      "key.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    props.put(
      "value.serializer",
      "org.apache.kafka.common.serialization.StringSerializer"
    )
    val producer = new KafkaProducer[String, String](props)
    val bufferedSource = io.Source.fromFile("D:\\IdeaProjects\\flink-test\\flink-test\\src\\main\\resources\\UserBehavior.csv")
    for (line <- bufferedSource.getLines) {
      val record = new ProducerRecord[String, String](topic, line)
      producer.send(record)
    }
    producer.close()
  }
}

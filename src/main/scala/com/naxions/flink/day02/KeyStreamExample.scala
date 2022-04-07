package com.naxions.flink.day02

import org.apache.flink.streaming.api.scala._

object KeyStreamExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource).filter( t => t.id.equals("sensor_1"))

    val keyStream: KeyedStream[SensorReading, String] = stream.keyBy(_.id)
    keyStream.print()

//    val maxStream = keyStream.max(2)
//    maxStream.print()

    env.execute()

  }

}

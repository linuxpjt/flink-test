package com.naxions.flink.day02

import org.apache.flink.streaming.api.scala._

object SensorStream {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream: DataStream[SensorReading] = env.addSource(new SensorSource)
    stream.print()

    env.execute()
  }

}

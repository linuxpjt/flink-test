package com.naxions.flink.day03

import com.naxions.flink.day02.{SensorReading, SensorSource}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow

object WindowExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream : DataStream[SensorReading] = env.addSource(new SensorSource)

    val keyStream : KeyedStream[SensorReading, String] = stream.keyBy(_.id)

    val windowStream : WindowedStream[SensorReading, String, TimeWindow]= keyStream.timeWindow(Time.seconds(10))

    val reduceStream : DataStream[SensorReading] = windowStream
      .reduce((r1, r2) => SensorReading(r1.id, 0L, r1.temperature.min(r2.temperature)))

    reduceStream.print()

    env.execute()
  }

}

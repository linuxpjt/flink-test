package com.naxions.flink.day03

import com.naxions.flink.day02.{SensorReading, SensorSource}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

// 全量聚合
object AvgTempByProcessWindowFunction {
  case class AvgInfo(id:String, avgTemp:Double, windowStart:Long, windowEnd:Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)
    stream
      .keyBy(_.id)
      .timeWindow(Time.seconds(5))
      .process(new AvgTempFunc)
      .print()

    env.execute()
  }

  class AvgTempFunc extends ProcessWindowFunction[SensorReading, AvgInfo, String, TimeWindow]{
    override def process(key: String, context: Context, elements: Iterable[SensorReading], out: Collector[AvgInfo]): Unit = {
      val count = elements.size
      var sum = 0.0

      for (elem <- elements) {
        sum += elem.temperature
      }
      val windowStart = context.window.getStart
      val windowEnd = context.window.getEnd
      out.collect(AvgInfo(key, sum/count, windowStart, windowEnd))
    }
  }

}

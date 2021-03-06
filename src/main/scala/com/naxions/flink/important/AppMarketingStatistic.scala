package com.naxions.flink.important

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector


// 不分渠道统计
object AppMarketingStatistic {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream = env
      .addSource(new SimulatcedEventSource)
      .assignAscendingTimestamps(_.ts)
      .filter(_.behaviour != "UNINSTALL")
      .map(r => ("key", 1L))
      .keyBy(_._1)
      .timeWindow(Time.seconds(5))
      .process(new WindowResult)

    stream.print()

    env.execute()
  }

  class WindowResult extends ProcessWindowFunction[(String, Long), (Long, Long), String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[(Long, Long)]): Unit = {
      out.collect((elements.size, context.window.getEnd))
    }
  }

}

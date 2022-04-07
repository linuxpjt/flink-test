package com.naxions.flink.day05

import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

object IntervalJoinExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    // 点击流
    val ClickStream = env
      .fromElements(
        ("user_1", "click", 3600 * 1000L),
        ("user_1", "click", 2400 * 1000L)
      )
      .assignAscendingTimestamps(_._3)
      .keyBy(_._1)

    // 浏览流
    val ScanStream = env
      .fromElements(
        ("user_1", "browse", 2000 * 1000L),
        ("user_1", "browse", 3100 * 1000L),
        ("user_1", "browse", 3200 * 1000L),
        ("user_1", "browse", 7200 * 1000L)
      )
      .assignAscendingTimestamps(_._3)
      .keyBy(_._1)

    // 两个流都做了keyBy，具有了相同的连接条件
    ClickStream
      .intervalJoin(ScanStream)
      .between(Time.seconds(-500), Time.seconds(0)) //条件筛选
      .process(new ProcessJoinFunction[(String, String, Long), (String, String, Long), String] {
        override def processElement(left: (String, String, Long), right: (String, String, Long), ctx: ProcessJoinFunction[(String, String, Long), (String, String, Long), String]#Context, out: Collector[String]) = {
          out.collect(left + " ======> " + right)
        }
      })
      .print()

    env.execute()

  }

}

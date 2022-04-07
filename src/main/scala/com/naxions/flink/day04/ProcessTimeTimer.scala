package com.naxions.flink.day04

import java.sql.Timestamp

import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object ProcessTimeTimer {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.socketTextStream("db2", 9999, '\n')

    stream
      .map(line => {
        val arr = line.split(" ")
        (arr(0), arr(1))
      })
      .keyBy(r => r._1)
      .process(new MyKeyed)
      .print()

    env.execute()
  }

  class MyKeyed extends KeyedProcessFunction[String, (String, String), String] {
    override def processElement(value: (String, String), ctx: KeyedProcessFunction[String, (String, String), String]#Context, out: Collector[String]): Unit = {
      // 注册一个定时器，当前机器时间+10s
      ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 10 * 1000L)
    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, (String, String), String]#OnTimerContext, out: Collector[String]): Unit = {
      out.collect("定时器触发了！触发时间是：" + new Timestamp(timestamp))
    }
  }
}

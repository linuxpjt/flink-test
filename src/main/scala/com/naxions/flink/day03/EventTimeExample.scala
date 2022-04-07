package com.naxions.flink.day03

import java.time.Duration

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector


object EventTimeExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    // 设置流的时间语义为事件时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream = env.socketTextStream("db2", 9999, '\n')

    stream
      .map(t => {
        val arr = t.split(" ")
        // 时间戳的单位是毫秒，所以需要ETL一下
        (arr(0), arr(1).toLong * 1000L)
      })
      .assignTimestampsAndWatermarks(
        // 水位线策略；默认200ms的机器时间插入一次水位线
        // 水位线 = 当前观察到的事件所携带的最大时间戳 - 最大延迟时间
        WatermarkStrategy
          .forBoundedOutOfOrderness[(String, Long)](Duration.ofSeconds(5))
          .withTimestampAssigner(new SerializableTimestampAssigner[(String, Long)] {
            override def extractTimestamp(element: (String, Long), recordTimestamp: Long) = element._2
          })
      )
      .keyBy(_._1)
      .timeWindow(Time.seconds(10))
      .process(new WinPro)
      .print()

    env.execute()

  }

  class WinPro extends ProcessWindowFunction[(String,Long), String, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[String]): Unit = {
      out.collect("key为：" + key +" 的窗口为 " + context.window.getStart + "----" + context.window.getEnd + " 中有 " + elements.size + " 个元素")
    }
  }

}

//################################################################################################33
//hello 1
//hello 5
//a 6
//a 9
//a 10
//a 14
//a 15
//a 20
//a 21
//a 25
//a 35
//a 45

//结果：
//key为：hello 的窗口为 0----10000 中有 2 个元素
//key为：a 的窗口为 0----10000 中有 2 个元素
//key为：a 的窗口为 10000----20000 中有 3 个元素
//key为：a 的窗口为 20000----30000 中有 3 个元素
//key为：a 的窗口为 30000----40000 中有 1 个元素

//################################################################################################33

//a 1
//a 8
//a 15
//a 1000
//a 1010
//a 1011
//a 1020
//a 1025
//a 1030
//a 1035
//a 2000

//结果：
//key为：a 的窗口为 0----10000 中有 2 个元素
//key为：a 的窗口为 10000----20000 中有 1 个元素
//key为：a 的窗口为 1000000----1010000 中有 1 个元素
//key为：a 的窗口为 1010000----1020000 中有 2 个元素
//key为：a 的窗口为 1020000----1030000 中有 2 个元素
//key为：a 的窗口为 1030000----1040000 中有 2 个元素
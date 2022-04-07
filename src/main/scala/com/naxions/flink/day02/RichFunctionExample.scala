package com.naxions.flink.day02

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala._

object RichFunctionExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.fromElements("hello world","hello haha")

    stream
      .map(new MyRichMap)
      .print()

    env.execute()
  }

  class MyRichMap extends RichMapFunction[String,String] {
    override def open(configuration: Configuration) = {
      println("开始生命周期")
    }

    override def close() = {
      println("结束生明周期")
    }

    override def map(in: String) = {
      val taskName = getRuntimeContext.getTaskName
      "任务名字是：" + taskName
    }
  }

}

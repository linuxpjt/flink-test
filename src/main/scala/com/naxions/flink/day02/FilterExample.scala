package com.naxions.flink.day02

import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.streaming.api.scala._

object FilterExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)

    // 接口实现
    stream.filter(t => t.id.equals("sensor_1")).print()
    // 匿名函数实现
    stream.filter(new FilterFunction[SensorReading] {
      override def filter(t: SensorReading) = t.id.equals("sensor_1")
    })
    // 抽象方法
    stream.filter(new MyfilterFunction).print()

    env.execute()
  }

  class MyfilterFunction extends FilterFunction[SensorReading] {
    override def filter(t: SensorReading) = t.id.equals("sensor_1")
  }

}

package com.naxions.flink.day02

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.streaming.api.scala._

object MapExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)
    // 接口实现
    val mapped1 = stream.map(t => t.id)
    // 抽象方法
    val mapped2 = stream.map(new MyMapFunction)
    // 匿名函数
    val mapped3 = stream.map(new MapFunction[SensorReading, String] {
      override def map(t: SensorReading) = t.id
    })

    mapped1.print()
    mapped2.print()
    mapped3.print()

    env.execute()
  }

  class MyMapFunction extends MapFunction[SensorReading, String] {
    override def map(t: SensorReading) = t.id
  }

}

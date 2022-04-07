package com.naxions.flink.day02

import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object CoFlatMapFunctionExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream1 = env
      .fromElements(
        (1, "aaaaa"),
        (2, "bbbbb")
      )

    val stream2 = env
      .fromElements(
        (1, "ccccc"),
        (2, "ddddd")
      )

    val connectedStream: ConnectedStreams[(Int, String), (Int, String)] = stream1.keyBy(_._1).connect(stream2.keyBy(_._1))

    connectedStream.flatMap(new MyFlatMapFunction).print()

    env.execute()
  }

  class MyFlatMapFunction extends CoFlatMapFunction[(Int, String), (Int, String), String]{
    override def flatMap1(value: (Int, String), out: Collector[String]) = {
      out.collect(value._2 + "来自第一条流的元素发送两次")
      out.collect(value._2 + "来自第一条流的元素发送两次")
    }

    override def flatMap2(value: (Int, String), out: Collector[String]) = {
      out.collect(value._2 + "来自第二条流的元素发送一次")
    }
  }
}

package com.naxions.flink.day05

import org.apache.flink.api.common.functions.JoinFunction
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

object WindowJoinExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val input1 = env.fromElements(
      ("a1", 10, 1000L),
      ("a1", 20, 2000L),
      ("b1", 10, 1000L),
      ("b2", 20, 2000L)
    )
      .assignAscendingTimestamps(_._3)

    val input2 = env.fromElements(
      ("a1", 30, 1000L),
      ("a1", 40, 2000L),
      ("b1", 30, 1000L),
      ("b2", 40, 2000L)
    )
      .assignAscendingTimestamps(_._3)

    input1
      .join(input2)
      .where(_._1)
      .equalTo(_._1)
      .window(TumblingEventTimeWindows.of(Time.seconds(10)))
      .apply(new MyJoin)
      .print()

    env.execute()
  }

  class MyJoin extends JoinFunction[(String, Int, Long), (String, Int, Long), String] {
    override def join(first: (String, Int, Long), second: (String, Int, Long)) = {
      first + " ==============> " + second
    }
  }

}

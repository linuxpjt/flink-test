package com.naxions.flink.day04

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object RedirectLateEventCustom {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream = env.socketTextStream("db2", 9999, '\n')

    val readings = stream
      .map( t => {
        val arr = t.split(" ")
        (arr(0), arr(1).toLong * 1000L)
      })
      .assignAscendingTimestamps(_._2)
      .process(new MyPro)

    readings.print()
    readings.getSideOutput(new OutputTag[(String)]("late")).print()

    env.execute()
  }

  class MyPro extends ProcessFunction[(String, Long), (String, Long)] {

    val late = new OutputTag[String]("late")

    override def processElement(value: (String, Long), ctx: ProcessFunction[(String, Long), (String, Long)]#Context, out: Collector[(String, Long)]): Unit = {
      if (value._2 < ctx.timerService().currentWatermark()) {
        ctx.output(late, "迟到时间来了！TS " + value._2)
      } else {
        out.collect(value)
      }
    }
  }

}

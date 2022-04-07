package com.naxions.flink.day03

import com.naxions.flink.day02.{SensorReading, SensorSource}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

// 增量聚合
object AvgTempByAggregateFunction {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)

    stream.keyBy(_.id)
      .timeWindow(Time.seconds(5))
      .aggregate(new AvgTempAgg)
      .print()

    env.execute()
  }

  class AvgTempAgg extends AggregateFunction[SensorReading, (String, Double, Long), (String,Double)] {
    override def createAccumulator() = ("", 0.0, 0L)

    override def add(value: SensorReading, accumulator: (String, Double, Long)) = {
      (value.id, accumulator._2 + value.temperature, accumulator._3 + 1)
    }

    override def getResult(accumulator: (String, Double, Long)) = {
      (accumulator._1, accumulator._2/accumulator._3)
    }

    override def merge(a: (String, Double, Long), b: (String, Double, Long)) = {
      (a._1, a._2+b._2, a._3+b._3)
    }
  }

}

package com.naxions.flink.proj

import java.sql.Timestamp

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.collection.mutable.Set

object UVPerWindowWithAggAndWindowProcess {

  case class UserBehavior(userId: String, itemId: String, categoryId: String, behavior: String, timestamp: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream = env
      .readTextFile("D:\\IdeaProjects\\flink-test\\flink-test\\src\\main\\resources\\UserBehavior.csv")
      .map(line => {
        val arr = line.split(",")
        UserBehavior(arr(0), arr(1), arr(2), arr(3), arr(4).toLong * 1000L)
      })
      .filter(r => r.behavior.equals("pv"))
      .assignAscendingTimestamps(_.timestamp)

    stream
      .map(r => ("key", r.userId))
      .keyBy(r => r._1)
      .timeWindow(Time.hours(1))
      .aggregate(new CountAgg, new WindowResult)
      .print()

    env.execute()
  }

  class CountAgg extends AggregateFunction[(String, String), (Set[String], Long), Long] {
    override def createAccumulator() = (Set[String](), 0L)

    override def add(value: (String, String), accumulator: (Set[String], Long)) = {
      if (!accumulator._1.contains(value._2)) {
        accumulator._1 += value._2
        (accumulator._1, accumulator._2 + 1)
      } else {
        accumulator
      }
    }

    override def getResult(accumulator: (Set[String], Long)) = {
      accumulator._2
    }

    override def merge(a: (Set[String], Long), b: (Set[String], Long)) = ???
  }

  class WindowResult extends ProcessWindowFunction[Long, String, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[Long], out: Collector[String]): Unit = {
      out.collect("window end: " + new Timestamp(context.window.getEnd) + " uv count: " + elements.head)
    }
  }

}

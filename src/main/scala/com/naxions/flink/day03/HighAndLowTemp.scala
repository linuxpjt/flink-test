package com.naxions.flink.day03

import com.naxions.flink.day02.{SensorReading, SensorSource}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object HighAndLowTemp {

  case class MinMaxTemp(id: String, minTemp: Double, maxTemp: Double, window: Double)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)
    stream.keyBy(_.id)
      .timeWindow(Time.seconds(5))
      .aggregate(new HighAndLowAgg, new WindowResult)
      .print()



    env.execute()
  }

  class HighAndLowAgg extends AggregateFunction[SensorReading, (String, Double, Double), (String, Double, Double)] {
    // 最小温度值的初始值是Double的最大值
    // 最大温度值的初始值是Double的最小值
    // 主要是后面比较过程中，数据流中的值能够直接替换累加器中的初始值，比如：数据流中的最小值肯定小于scala中Double的最大值，所以数据流中的值，肯定会替换累加器中的初始值，同理求最大值时，数据流最大值也会与scala中Double的最小值替换
    override def createAccumulator() = ("", Double.MaxValue, Double.MinValue)

    override def add(value: SensorReading, accumulator: (String, Double, Double)) = {
      (value.id, value.temperature.min(accumulator._2), value.temperature.max(accumulator._3))
    }

    override def getResult(accumulator: (String, Double, Double)) = accumulator

    override def merge(a: (String, Double, Double), b: (String, Double, Double)) = {
      (a._1, a._2.min(b._2), a._3.max(b._3))
    }
  }

  class WindowResult extends ProcessWindowFunction[(String, Double, Double), MinMaxTemp, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[(String, Double, Double)], out: Collector[MinMaxTemp]): Unit = {
      val minMax = elements.head
      out.collect(MinMaxTemp(key, minMax._2, minMax._3, context.window.getEnd))
    }
  }
}

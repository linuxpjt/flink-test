package com.naxions.flink.important

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Duration

import org.apache.flink.api.common.eventtime.{SerializableTimestampAssigner, WatermarkStrategy}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.state.ListStateDescriptor
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.collection.mutable.ListBuffer

object ApacheLogAnalysis {

  case class ApacheLog(ipAdd: String,
                       userId: String,
                       eventTime: Long,
                       method: String,
                       url: String)

  case class UrlViewCount(url: String,
                          windowEnd: Long,
                          count: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)


    val stream = env
      .readTextFile("D:\\IdeaProjects\\flink-test\\flink-test\\src\\main\\resources\\apache.log")
      .map(line => {
        val arr = line.split(" ")
        val simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss")
        val ts = simpleDateFormat.parse(arr(3)).getTime
        ApacheLog(arr(0), arr(2), ts, arr(5), arr(6))
      })
//      .assignAscendingTimestamps(_.eventTime)
      .assignTimestampsAndWatermarks(
        WatermarkStrategy
          .forBoundedOutOfOrderness[ApacheLog](Duration.ofSeconds(5))
          .withTimestampAssigner(new SerializableTimestampAssigner[ApacheLog] {
            override def extractTimestamp(element: ApacheLog, recordTimestamp: Long) = element.eventTime
          })
      )
      .keyBy(_.url)
      .timeWindow(Time.minutes(1), Time.seconds(5))
      .aggregate(new CountAgg, new WindowResult)
      .keyBy(_.windowEnd)
      .process(new TopNUrl)

    stream.print()

    env.execute()
  }

  class TopNUrl extends KeyedProcessFunction[Long, UrlViewCount, String] {
    lazy val itemState = getRuntimeContext.getListState(
      new ListStateDescriptor[UrlViewCount]("item-state", Types.of[UrlViewCount])
    )

    override def processElement(value: UrlViewCount, ctx: KeyedProcessFunction[Long, UrlViewCount, String]#Context, out: Collector[String]) = {
      itemState.add(value)
      ctx.timerService().registerEventTimeTimer(value.windowEnd + 100L)
    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[Long, UrlViewCount, String]#OnTimerContext, out: Collector[String]): Unit = {
      val allItems = new ListBuffer[UrlViewCount]
      import scala.collection.JavaConversions._
      for (item <- itemState.get()) {
        allItems += item
      }
      itemState.clear()

      val sortItems = allItems.sortBy(-_.count).take(3)
      val result = new StringBuilder
      result.append("=======================================================================\n")
      result.append("时间：").append(new Timestamp(timestamp - 100)).append("\n")
      // indices代表长度
      for (i <- sortItems.indices) {
        val currentItem = sortItems(i)
        result.append("No")
          .append(i + 1)
          .append(" : ")
          .append("  url =  ")
          .append(currentItem.url)
          .append("  流量 = ")
          .append(currentItem.count)
          .append("\n")
      }

      result.append("=======================================================================\n")
      Thread.sleep(1000)
      out.collect(result.toString())
    }

  }


  class CountAgg extends AggregateFunction[ApacheLog, Long, Long] {
    override def createAccumulator() = 0L

    override def add(value: ApacheLog, accumulator: Long) = accumulator + 1

    override def getResult(accumulator: Long) = accumulator

    override def merge(a: Long, b: Long) = a + b
  }

  class WindowResult extends ProcessWindowFunction[Long, UrlViewCount, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[Long], out: Collector[UrlViewCount]): Unit = {
      out.collect(UrlViewCount(key, context.window.getEnd, elements.head))
    }
  }


}

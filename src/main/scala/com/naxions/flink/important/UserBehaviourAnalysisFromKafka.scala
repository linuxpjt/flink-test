package com.naxions.flink.important

import java.sql.Timestamp
import java.util.Properties

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.state.ListStateDescriptor
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.util.Collector

import scala.collection.mutable.ListBuffer

object UserBehaviourAnalysisFromKafka {
  case class UserBehavior(userId: String, itemId: String, categoryId: String, behavior: String, timestamp: Long)

  case class ItemViewCount(itemId: String, windowEnd: Long, count: Long)


  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "db2:9092")
    properties.setProperty("group.id", "consumer-group")
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("auto.offset.reset", "latest")

    val stream = env
      .addSource(new FlinkKafkaConsumer011[String](
        "test2021",
        new SimpleStringSchema(),
        properties
      ))
      .map(line => {
        val arr = line.split(",")
        UserBehavior(arr(0), arr(1), arr(2), arr(3), arr(4).toLong * 1000L)
      })
      .filter(r => r.behavior.equals("pv"))
      .assignAscendingTimestamps(_.timestamp)

    stream
      .keyBy(r => r.itemId)
      .timeWindow(Time.hours(1), Time.minutes(5))
      .aggregate(new CountAgg, new WindowResult)
      .keyBy(_.windowEnd)
      .process(new TopNItems(3))
      .print()

    env.execute()
  }



  class TopNItems(n: Int) extends KeyedProcessFunction[Long, ItemViewCount, String] {
    lazy val itemState = getRuntimeContext.getListState(
      new ListStateDescriptor[ItemViewCount]("item-state", Types.of[ItemViewCount])
    )

    override def processElement(value: ItemViewCount, ctx: KeyedProcessFunction[Long, ItemViewCount, String]#Context, out: Collector[String]) = {
      itemState.add(value)
      ctx.timerService().registerEventTimeTimer(value.windowEnd + 100L)
    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[Long, ItemViewCount, String]#OnTimerContext, out: Collector[String]): Unit = {
      val allItems = new ListBuffer[ItemViewCount]()
      import scala.collection.JavaConversions._
      for (item <- itemState.get()) {
        allItems += item
      }
      itemState.clear()

      val sortItems = allItems.sortBy(-_.count).take(n)
      val result = new StringBuilder
      result.append("=======================================================================\n")
      result.append("时间：").append(new Timestamp(timestamp - 100)).append("\n")
      // indices代表长度
      for (i <- sortItems.indices) {
        val currentItem = sortItems(i)
        result.append("No")
          .append(i + 1)
          .append(" : ")
          .append("  商品ID =  ")
          .append(currentItem.itemId)
          .append("  浏览量 = ")
          .append(currentItem.count)
          .append("\n")
      }
      result.append("=======================================================================\n")
      Thread.sleep(1000)
      out.collect(result.toString())
    }


  }


  class CountAgg extends AggregateFunction[UserBehavior, Long, Long] {
    override def createAccumulator(): Long = 0L

    override def add(in: UserBehavior, acc: Long): Long = acc + 1

    override def getResult(acc: Long): Long = acc

    override def merge(acc: Long, acc1: Long): Long = ???
  }

  class WindowResult extends ProcessWindowFunction[Long, ItemViewCount, String, TimeWindow] {

    override def process(key: String, context: Context, elements: Iterable[Long], out: Collector[ItemViewCount]): Unit = {
      out.collect(ItemViewCount(key, context.window.getEnd, elements.head))
    }
  }
}

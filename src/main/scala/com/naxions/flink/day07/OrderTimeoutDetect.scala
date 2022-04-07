package com.naxions.flink.day07

import org.apache.flink.cep.scala.CEP
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

import scala.reflect.internal.util.Collections

object OrderTimeoutDetect {
  case class OrderEvent(orderId: String, eventType: String, eventTime: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val timeoutputTag = new OutputTag[String]("timeout-tag")

    val orderStream = env
      .fromElements(
        OrderEvent("order_1", "create", 2000L),
        OrderEvent("order_2", "create", 3000L),
        OrderEvent("order_1", "pay", 4000L),
        OrderEvent("order_2", "pay", 10000L)
      )
      .assignAscendingTimestamps(_.eventTime)
      .keyBy(_.orderId)

    val pattern = Pattern
      .begin[OrderEvent]("create")
      .where(_.eventType.equals("create"))
      .next("pay")
      .where(_.eventType.equals("pay"))
      .within(Time.seconds(5))

    val patternedStream = CEP.pattern(orderStream, pattern)

    // 匿名函数
    val selectFunc = (map: scala.collection.Map[String, Iterable[OrderEvent]], out: Collector[String]) => {
      val create = map("create").iterator.next()
      out.collect("order id " + create.orderId + " is payed")
    }

    // 匿名函数
    val timeoutFunc = (map: scala.collection.Map[String, Iterable[OrderEvent]], ts: Long, out: Collector[String]) => {
      val create = map("create").iterator.next()
      out.collect("order id " + create.orderId + " is not payed and timeout is " + ts)
    }

    // flatSelect可以输出多个结果，所以可以使用out输出多行
    val selectStream = patternedStream.flatSelect(timeoutputTag)(timeoutFunc)(selectFunc)

    selectStream.print()
    selectStream.getSideOutput(timeoutputTag).print()

    env.execute()

  }


}

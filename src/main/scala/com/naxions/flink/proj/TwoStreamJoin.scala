package com.naxions.flink.proj

import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object TwoStreamJoin {

  case class OrderEvent(orderId: String, eventType: String, eventTime: Long)

  case class PayEvent(orderId: String, eventType: String, eventTime: Long)

  val unmatchedOrders = new OutputTag[String]("unmatched-orders")
  val unmatchedPays = new OutputTag[String]("unmatched-pays")

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val orderStream = env
      .fromElements(
        OrderEvent("order_1", "pay", 1000L),
        OrderEvent("order_2", "pay", 2000L)
      )
      .assignAscendingTimestamps(_.eventTime)
      .keyBy(_.orderId)

    val payStream = env
      .fromElements(
        PayEvent("order_1", "weixin", 3000L),
        PayEvent("order_3", "weixin", 4000L)
      )
      .assignAscendingTimestamps(_.eventTime)
      .keyBy(_.orderId)

    val result = orderStream
      .connect(payStream)
      .process(new MatchFunction)


    result.print()
    result.getSideOutput(unmatchedOrders).print()
    result.getSideOutput(unmatchedPays).print()

    env.execute()

  }

  class MatchFunction extends CoProcessFunction[OrderEvent, PayEvent, String] {

    lazy val orderState = getRuntimeContext.getState(
      new ValueStateDescriptor[OrderEvent]("order", Types.of[OrderEvent])
    )

    lazy val payState = getRuntimeContext.getState(
      new ValueStateDescriptor[PayEvent]("pay", Types.of[PayEvent])
    )

    override def processElement1(order: OrderEvent, ctx: CoProcessFunction[OrderEvent, PayEvent, String]#Context, out: Collector[String]) = {
      val pay = payState.value()

      if (pay != null) {
        payState.clear()
        out.collect("order id: " + order.orderId + " matched sucess!")
      } else {
        orderState.update(order)
        ctx.timerService().registerEventTimeTimer(order.eventTime + 5000L)
      }
    }

    override def processElement2(pay: PayEvent, ctx: CoProcessFunction[OrderEvent, PayEvent, String]#Context, out: Collector[String]) = {
      val order = orderState.value()

      if (order != null) {
        orderState.clear()
        out.collect("order id: " + pay.orderId + " matched sucess!")
      } else {
        payState.update(pay)
        ctx.timerService().registerEventTimeTimer(pay.eventTime + 5000L)
      }
    }

    override def onTimer(timestamp: Long, ctx: CoProcessFunction[OrderEvent, PayEvent, String]#OnTimerContext, out: Collector[String]): Unit = {
      if (orderState.value() != null) {
        ctx.output(unmatchedOrders, "order id: " + orderState.value().orderId + " failed match")
        orderState.clear()
      }

      if (payState.value() != null) {
        ctx.output(unmatchedPays, "order id: " + payState.value().orderId + " failed match")
        payState.clear()
      }
    }

  }

}

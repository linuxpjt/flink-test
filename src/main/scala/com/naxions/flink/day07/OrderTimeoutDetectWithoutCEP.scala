package com.naxions.flink.day07

import java.lang.Thread.sleep

import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.cep.scala.CEP
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

object OrderTimeoutDetectWithoutCEP {
  case class OrderEvent(orderId: String, eventType: String, eventTime: Long)

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val orderStream = env
      .fromElements(
        OrderEvent("order_1", "create", 2000L),
        OrderEvent("order_2", "create", 3000L),
        OrderEvent("order_1", "pay", 4000L)
      )
      .assignAscendingTimestamps(_.eventTime)
      .keyBy(_.orderId)
      .process(new OrderMatch)

    orderStream.print()

    sleep(10000)

    env.execute()

  }

  class OrderMatch extends KeyedProcessFunction[String, OrderEvent, String] {
    lazy val orderState = getRuntimeContext.getState(
      new ValueStateDescriptor[OrderEvent]("order-event", Types.of[OrderEvent])
    )

    override def processElement(value: OrderEvent, ctx: KeyedProcessFunction[String, OrderEvent, String]#Context, out: Collector[String]) = {
      if (value.eventType.equals("create")) {
        if (orderState.value() == null) {
          orderState.update(value)
          ctx.timerService().registerEventTimeTimer(value.eventTime + 15000L)
        } else {
          out.collect("order id " + value.orderId + " is payed")
        }
      } else {
        orderState.update(value)
        out.collect("order id " + value.orderId + " is payed ")
      }
    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, OrderEvent, String]#OnTimerContext, out: Collector[String]): Unit = {
      val saveOrder = orderState.value()

      if (saveOrder != null && saveOrder.eventType.equals("create")) {
        out.collect("order id " + ctx.getCurrentKey + " is not payed")
      }

      orderState.clear()
    }
  }

}

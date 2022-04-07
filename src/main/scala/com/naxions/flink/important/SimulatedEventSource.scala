package com.naxions.flink.important

import java.util.{Calendar, UUID}

import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, SourceFunction}

import scala.util.Random

class SimulatcedEventSource extends RichParallelSourceFunction[MarketingUserBehaviour] {
  var running = true
  val channelSet = Seq("AppStore", "XiaomiStore", "HuaweiStore")

  val behaviourTypes = Seq("BROWSE", "CLICK", "INSTALL", "UNINSTALL")

  val rand = new Random

  override def run(ctx: SourceFunction.SourceContext[MarketingUserBehaviour]): Unit = {
    while (running) {
      val userId = UUID.randomUUID().toString
      val behaviourType = behaviourTypes(rand.nextInt(behaviourTypes.size))
      val channel = channelSet(rand.nextInt(channelSet.size))
      val ts = Calendar.getInstance().getTimeInMillis

      ctx.collect(MarketingUserBehaviour(userId = userId, behaviour = behaviourType, channel = channel, ts = ts))
      Thread.sleep(10)
    }
  }

  override def cancel() = running = false
}

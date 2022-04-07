package com.naxions.flink.day02

import java.util.Calendar

import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, SourceFunction}

import scala.collection.immutable
import scala.util.Random

class SensorSource extends RichParallelSourceFunction[SensorReading] {
  var running = true

  override def run(sourceContext: SourceFunction.SourceContext[SensorReading]): Unit = {
    val random: Random = new Random()

    val curFTemp: immutable.IndexedSeq[(String, Double)] = (1 to 10).map(
      i => ("sensor_" + i, random.nextGaussian() * 20)
    )

    while (running) {
      val curFtemp: immutable.IndexedSeq[(String, Double)] = curFTemp.map(
        t => (t._1, t._2 + random.nextGaussian() * 0.5)
      )
      val curTime: Long = Calendar.getInstance.getTimeInMillis

      curFtemp.foreach(t => sourceContext.collect(SensorReading(t._1, curTime, t._2)))

      Thread.sleep(1000)
    }
  }

  override def cancel(): Unit = running = false
}

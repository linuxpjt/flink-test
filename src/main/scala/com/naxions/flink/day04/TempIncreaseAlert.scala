package com.naxions.flink.day04

import com.naxions.flink.day02.{SensorReading, SensorSource}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object TempIncreaseAlert {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)

    stream
      .keyBy(_.id)
      .process(new TempIncrease)
      .print()

    env.execute()
  }

  class TempIncrease extends KeyedProcessFunction[String, SensorReading, String] {
    // 懒加载；
    // 状态变量会在检查点操作时进行持久化，例如hdfs
    // 只会初始化一次，单例模式
    // 在当机重启程序时，首先去持久化设备寻找名为`last-temp`的状态变量，如果存在，则直接读取。不存在，则初始化。
    // 用来保存最近一次温度
    // 默认值是0.0
    lazy val lastTemp: ValueState[Double] = getRuntimeContext.getState(
      new ValueStateDescriptor[Double]("last-temp", Types.of[Double])
    )

    // 默认值是0L，用来保存报警的时间戳
    lazy val curTimeTs: ValueState[Long] = getRuntimeContext.getState(
      new ValueStateDescriptor[Long]("curTimeTs", Types.of[Long])
    )

    override def processElement(value: SensorReading, ctx: KeyedProcessFunction[String, SensorReading, String]#Context, out: Collector[String]) = {
      val preTemp = lastTemp.value()
      lastTemp.update(value.temperature)

      val ts = curTimeTs.value()

      if (preTemp == 0.0 || value.temperature < preTemp) {
        ctx.timerService().deleteEventTimeTimer(ts)
        curTimeTs.clear()
      } else if (value.temperature>preTemp && ts == 0) {
        val onSecondlater = ctx.timerService().currentProcessingTime() + 1000L
        ctx.timerService().registerProcessingTimeTimer(onSecondlater)
        curTimeTs.update(onSecondlater)
      }
    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, SensorReading, String]#OnTimerContext, out: Collector[String]): Unit = {
      out.collect("传感器ID是 " + ctx.getCurrentKey + " 的传感器的温度连续1s上升了！")
      curTimeTs.clear()
    }

  }

}

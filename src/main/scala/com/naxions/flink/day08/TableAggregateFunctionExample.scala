package com.naxions.flink.day08

import com.naxions.flink.day02.SensorSource
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.TableAggregateFunction
import org.apache.flink.types.Row
import org.apache.flink.util.Collector

object TableAggregateFunctionExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource).filter(_.id.equals("sensor_1"))

    val settings = EnvironmentSettings
      .newInstance()
      .inStreamingMode()
      .build()

    val tableEnv = StreamTableEnvironment.create(env, settings)

    val top2Temp = new Top2Temp()

    // table
    val table = tableEnv.fromDataStream(stream, $"id", $"timestamp" as "ts", $"temperature" as "temp")

    val tableResult = table
      .groupBy($"id")
      .flatAggregate(top2Temp($"temp") as("t", "rank"))
      .select($"id", $"t", $"rank")

    tableEnv
      .toRetractStream[Row](tableResult)
      .print()

    // sql
//    tableEnv.createTemporaryView("t", table)
//
//    tableEnv.registerFunction("top2Temp", top2Temp)
//
//    val sqlReault = tableEnv
//      .sqlQuery(
//        """
//          |select id,top2Temp(temp) as T(temp,rank)
//          |from t
//          |group by id
//          |""".stripMargin)
//
//    tableEnv
//      .toRetractStream[Row](sqlReault)
//      .print()



    env.execute()
  }

  // 累加器
  class Top2TempAcc {
    var higestTemp: Double = Double.MinValue
    var secondTemp: Double = Double.MinValue
  }

  // 第一个泛型是（温度值,排名）
  // 第一个泛型是累加器
  class Top2Temp extends TableAggregateFunction[(Double, Int), Top2TempAcc] {
    override def createAccumulator() = new Top2TempAcc

    // 函数名必须是accumulate，运行时会编译检查
    def accumulate(acc: Top2TempAcc, temp: Double) = {
      if (temp > acc.higestTemp) {
        acc.secondTemp = acc.higestTemp
        acc.higestTemp = temp
      } else if (temp > acc.secondTemp) {
        acc.secondTemp = temp
      }
    }

    // 函数名必须是emitValue，用来发射计算结果
    def emitValue(acc: Top2TempAcc, out: Collector[(Double, Int)]) = {
      out.collect(acc.higestTemp, 1)
      out.collect(acc.secondTemp, 2)
    }

  }

}

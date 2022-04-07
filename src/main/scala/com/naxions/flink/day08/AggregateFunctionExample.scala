package com.naxions.flink.day08

import com.naxions.flink.day02.SensorSource
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.{StreamTableEnvironment, table2RowDataStream}
import org.apache.flink.table.functions.AggregateFunction
import org.apache.flink.types.Row


// 聚合函数结果是一个标量值
object AggregateFunctionExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource).filter(_.id.equals("sensor_1"))

    val settings = EnvironmentSettings
      .newInstance()
      .inStreamingMode()
      .build()

    val tableEnv = StreamTableEnvironment.create(env, settings)

    val avgTemp = new AvgTemp()

    // table
    val table = tableEnv.fromDataStream(stream, $"id", $"timestamp" as "ts", $"temperature" as "temp")
    val tableResult = table
      .groupBy($"id")
      .aggregate(avgTemp($"temp") as "avgTemp")
      .select($"id", $"avgTemp")

    tableEnv
      .toRetractStream[Row](tableResult)
//      .print()

    // sql
    tableEnv.createTemporaryView("sensor", table)

    tableEnv.registerFunction("avgTemp", avgTemp)

    val sqlResult = tableEnv
      .sqlQuery(
        """
          |select
          | id,avgTemp(temp)
          |from sensor
          |group by id
          |""".stripMargin
      )
    tableEnv
      .toRetractStream[Row](sqlResult).print()



    env.execute()
  }

  // 累加器的类型
  class AvgTempAcc {
    var sum: Double = 0.0
    var count: Int = 0
  }

  // 第一个泛型是温度的类型
  // 第二个泛型是累加器的类型
  class AvgTemp extends AggregateFunction[Double, AvgTempAcc] {
    override def createAccumulator() = new AvgTempAcc

    // 累加规则
    def accumulate(acc: AvgTempAcc, temp: Double) = {
      acc.sum += temp
      acc.count += 1
    }

    override def getValue(acc: AvgTempAcc) = {
      acc.sum / acc.count
    }
  }

}

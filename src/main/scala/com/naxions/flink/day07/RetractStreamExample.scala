package com.naxions.flink.day07

import com.naxions.flink.day02.SensorSource
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row

object RetractStreamExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val settings = EnvironmentSettings
      .newInstance()
      .inStreamingMode()
      .build()

    val tableEnv = StreamTableEnvironment.create(env, settings)

    val stream = env.addSource(new SensorSource)

    // sql api
    tableEnv.createTemporaryView("sensor", stream)

    val result = tableEnv
      .sqlQuery("select id,count(1) from sensor where id = 'sensor_1' group by id")

    tableEnv
      .toRetractStream[Row](result)
      .print()

    // table api
    val table = tableEnv.fromDataStream(stream)

    val tableResult = table
      .filter($"id" === "sensor_1")
      .groupBy($"id")
      .select($"id", $"id".count())

    tableEnv
      .toRetractStream[Row]{tableResult}
      .print()


    env.execute()
  }

}

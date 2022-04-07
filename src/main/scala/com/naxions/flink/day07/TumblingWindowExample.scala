package com.naxions.flink.day07

import com.naxions.flink.day02.SensorSource
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.types.Row

object TumblingWindowExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val settings = EnvironmentSettings
      .newInstance()
      .inStreamingMode()
      .build()

    val tableEnv = StreamTableEnvironment.create(env, settings)

    val stream = env.addSource(new SensorSource).filter(_.id.equals("sensor_1"))

    // table api
    val table = tableEnv.fromDataStream(stream, $"id", $"timestamp" as "ts", $"pt".proctime())

    val tableResult = table
      .window(Tumble over 10.seconds() on $"pt" as $"w")
      .groupBy($"id", $"w")
      .select($"id", $"id".count())

    tableEnv.toRetractStream[Row](tableResult).print()

    // sql api
    tableEnv.createTemporaryView("sensor", stream, $"id", $"timestamp" as "ts", $"temperature", $"pt".proctime())

    val sqlResult = tableEnv
      .sqlQuery("select id,count(id), tumble_start(pt, interval '10' second), tumble_end(pt, interval '10' second) from sensor group by id,tumble(pt, interval '10' second)")

    tableEnv
      .toRetractStream[Row](sqlResult)
      .print()


    env.execute()

  }

}

package com.naxions.flink.day08

import com.naxions.flink.day02.SensorSource
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.ScalarFunction
import org.apache.flink.types.Row


// 标量聚合函数，只能返回单个值
object ScalarFunctionExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)

    val settings = EnvironmentSettings
      .newInstance()
      .inStreamingMode()
      .build()

    val tableEnv = StreamTableEnvironment.create(env, settings)

    val hashCode = new HashCode(10)

    // table
    val table = tableEnv.fromDataStream(stream)

//    val tableResult = table
//      .select($"id", hashCode($"id"))
//
//    tableEnv.toAppendStream[Row](tableResult).print()

    // sql
    // 这里使用createTemporarySystemFunction注册udf
    tableEnv.createTemporarySystemFunction("hashCode", hashCode)
    tableEnv.createTemporaryView("sensor", table)
    val sqlResult = tableEnv
      .sqlQuery("select id, hashCode(id) from sensor")

    tableEnv
      .toAppendStream[Row](sqlResult)
      .print()

    env.execute()
  }

  class HashCode(factor: Int) extends ScalarFunction {
    def eval(s: String): Int = {
      s.hashCode * factor
    }
  }

}

package com.naxions.flink.day08

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.functions.TableFunction
import org.apache.flink.types.Row


// 表函数TableFunction可以返回任意行个输出结果
object TableFunctionExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.fromElements(
      "hello#world",
      "naxions#data"
    )

    val settings = EnvironmentSettings
      .newInstance()
      .inStreamingMode()
      .build()

    val tableEnv = StreamTableEnvironment.create(env, settings)

    val table = tableEnv.fromDataStream(stream, $"s")

    // table
    val split = new Split("#")

    val tableResult = table
      .joinLateral(split($"s") as ("word", "length"))
      .select($"s", $"word", $"length")

    tableEnv
      .toAppendStream[Row](tableResult)
//      .print()

    // sql
    // 这里使用registerFunction注册udf
    tableEnv.registerFunction("split", new Split("#"))
    tableEnv.createTemporaryView("t", table)

    val sqlResult = tableEnv
      .sqlQuery("select s,word,length from t left join lateral table(split(s)) as T(word, length) on true ")
//      .sqlQuery("select s,word,length from t,lateral table(split(s)) as T(word, length)")

    tableEnv
      .toAppendStream[Row](sqlResult)
      .print()


    env.execute()

  }

  // 输出泛型是（String, Int）
  class Split(sep: String) extends TableFunction[(String, Int)] {
    def eval(s: String) {
      s.split(sep).foreach(x => collect(x, x.length))
    }
  }

}

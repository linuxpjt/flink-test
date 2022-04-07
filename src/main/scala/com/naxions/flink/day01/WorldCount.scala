package com.naxions.flink.day01

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object WorldCount {
  def main(args: Array[String]): Unit = {
    // 获取运行环境，类似SparkContext
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    // 设置分区
    env.setParallelism(1)

    // 建立元数据
    val stream: DataStream[String] = env.socketTextStream("db2", 9999, '\n')

    val transformed: DataStream[WordWithCount] = stream
      // 使用空格切分数据字符串
      .flatMap(line => line.split("\\s"))
      // 类似mr中map
      .map(w => WordWithCount(w, 1))
      // 使用world字段进行分组，1.10之前为keyBy（已弃用）
      .keyBy(t => t.word)
      // 开了一个5s的滚动窗口
      .timeWindow(Time.seconds(5))
      // 对聚合后的数据字段做累加操作，类似MR中的reduce
      .sum(1)

    transformed.print()

    env.execute()

  }

  case class WordWithCount(word: String, count: Long)

}

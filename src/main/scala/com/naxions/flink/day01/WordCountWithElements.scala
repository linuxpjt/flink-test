package com.naxions.flink.day01

import org.apache.flink.streaming.api.scala._

object WordCountWithElements {

  case class WordWithCount(word: String, count: Int)

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream: DataStream[String] = env.fromElements(
      "hello world",
      "hello world",
      "hello world",
      "hello"
    )

    val transformed: DataStream[WordWithCount] = stream
      .flatMap(line => line.split("\\s"))
      .map(w => WordWithCount(w, 1))
      .keyBy(w => w.word)
      .sum(1)

    transformed.print()

    env.execute()
  }

}

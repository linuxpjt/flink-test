package com.naxions.flink

object test {
  def main(args: Array[String]): Unit = {
    val r = scala.util.Random
//    val obj = r.nextInt(5)

    while (true) {
      println(r.nextInt(5))
    }

  }

}

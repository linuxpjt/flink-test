package com.naxions.flink.day06

import java.sql.{Connection, DriverManager, PreparedStatement}

import com.naxions.flink.day02.{SensorReading, SensorSource}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala._

object WriteToMysql {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)

    stream.addSink(new MyJDBCSink)

    env.execute()
  }

  class MyJDBCSink extends RichSinkFunction[SensorReading] {
    var conn: Connection = _
    var insertStmt: PreparedStatement = _
    var updateStmt: PreparedStatement = _

    override def open(parameters: Configuration): Unit = {
      conn = DriverManager.getConnection(
        "jdbc:mysql://am-2zew8935g4xfr65hn90650o.ads.aliyuncs.com:3306/test01",
        "nx_user_a",
        "Nax1@#$56go"
      )
      insertStmt = conn.prepareStatement("INSERT INTO z_pjt_01 (id, temp) VALUES (?, ?)")
      updateStmt = conn.prepareStatement("UPDATE z_pjt_01 SET temp = ? WHERE id = ?")
    }

    override def invoke(value: SensorReading, context: SinkFunction.Context[_]): Unit = {
      updateStmt.setDouble(1, value.temperature)
      updateStmt.setString(2, value.id)
      updateStmt.execute()

      if (updateStmt.getUpdateCount == 0) {
        insertStmt.setString(1, value.id)
        insertStmt.setDouble(2, value.temperature)
        insertStmt.execute()
      }
    }

    override def close(): Unit = {
      insertStmt.close()
      updateStmt.close()
      conn.close()
    }
  }
}

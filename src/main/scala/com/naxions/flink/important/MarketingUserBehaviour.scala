package com.naxions.flink.important

case class MarketingUserBehaviour(
                                 userId: String,
                                 behaviour: String,
                                 channel: String,
                                 ts: Long
                                 )

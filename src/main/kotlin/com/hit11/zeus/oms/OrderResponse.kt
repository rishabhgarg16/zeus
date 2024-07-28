package com.hit11.zeus.oms

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
class TradeResponse(
    val matchId: Int = 0,
    val userId: Int = 0,
    val pulseId: Int = 0,
    val pulseDetail: String = "",
    val price: BigDecimal = BigDecimal.valueOf(0),
    val userAnswer: String = "",
    val answerTime: Instant = Instant.now(),
    val tradeResult: String = "",
    val isPulseActive: Boolean = true,
    val pulseImageUrl: String? = "",
    val pulseEndDate: Instant? = Instant.now(),
    val userTradeQuantity: Long = 0L,
    val category: List<String> = emptyList(),
    val totalTraders: Long = 0L
)
package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UserTradeSubmissionRequest(
    val userIdRef: String = "",
    val pulseIdRef: String = "",
//    val matchIdRef: String = "",
    val userAnswer: String = "",
    val userWager: Double = -1.0,
    val userTradeQuantity: Long = 0L,
)
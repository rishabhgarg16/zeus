package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class UserPulseSubmissionResponse(
    val userId: String = "",
    val matchIdRef: String = "",
    val pulseId: String = "",
    val pulseDetail: String = "",
    val pulseText: String = "",
    val userWager: Double = -1.0,
    val userAnswer: String = "",
    val answerTime: Long = -1L,
    val userResult: String = "",
    val isPulseActive: Boolean = true,
    val pulseImageUrl: String = "",
    val pulseEndDate: Long = -1L,
    val userACount: Long = 10,
    val userBCount: Long = 10
)
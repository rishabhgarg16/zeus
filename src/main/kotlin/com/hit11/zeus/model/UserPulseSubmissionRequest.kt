package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserPulseSubmissionRequest(
    val userId: String = "",
    val pulseId: String = "",
    val matchIdRef: String = "",
    val userAnswer: String = "",
    val answerTime: Long = -1L,
    val userWager: Double = -1.0,
    val userResult: String = "",
    val isPulseActive: Boolean = false,
    val test: Boolean = false,
)
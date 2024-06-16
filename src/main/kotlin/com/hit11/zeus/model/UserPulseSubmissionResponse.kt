package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
class UserPulseSubmissionResponse(
    val userId: Int = 0,
    val matchId: Int = 0,
    val pulseId: Int = 0,
    val pulseDetail: String = "",
    val userWager: Double = -1.0,
    val userAnswer: String = "",
    val answerTime: Instant = Instant.now(),
    val userResult: String = "",
    val isPulseActive: Boolean = true,
    val pulseImageUrl: String? = "",
    val pulseEndDate: Instant? = Instant.now(),
    val userACount: Long? = 10,
    val userBCount: Long? = 10
)
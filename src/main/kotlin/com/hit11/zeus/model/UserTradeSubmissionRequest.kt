package com.hit11.zeus.model

import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class UserTradeSubmissionRequest(

    @field:NotNull(message = "User ID cannot be null")
    @field:Min(value = 1, message = "User ID must be greater than 0")
    val userId: Int = 0,

    @field:NotNull(message = "Pulse ID cannot be null")
    @field:Min(value = 1, message = "Pulse ID must be greater than 0")
    val pulseId: Int = 0,

    @field:NotNull(message = "Match ID cannot be null")
    @field:Min(value = 1, message = "Match ID must be greater than 0")
    val matchId: Int = 0,

    @field:NotBlank(message = "User Answer cannot be blank")
    val userAnswer: String = "",

    @field:NotNull(message = "Answer Time cannot be null")
    val answerTime: Long = -1L,

    @field:NotNull(message = "User Wager cannot be null")
    @field:Min(value = 1, message = "User Wager must be greater than 0")
    val userWager: Double = -1.0,

    @field:Min(value = 1, message = "User Quantity must be greater than 0")
    val userTradeQuantity: Long = 0L,
)
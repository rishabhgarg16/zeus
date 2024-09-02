package com.hit11.zeus.model

import java.time.Instant
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class OrderRequest(

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

    @field:NotNull(message = "User Wager cannot be null")
    @field:Min(value = 1, message = "User Wager must be greater than 0")
    val price: Double = -1.0,

    @field:Min(value = 1, message = "User Quantity must be greater than 0")
    val quantity: Long = 0L,

    val orderType: OrderType = OrderType.BUY,

    val executionType: OrderExecutionType = OrderExecutionType.MARKET,

    val createdAt: Instant = Instant.now(),
)
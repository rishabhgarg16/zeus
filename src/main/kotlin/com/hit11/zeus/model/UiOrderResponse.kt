package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant

class UiOrderResponse(
    val id: Long = 0,
    val matchId: Int = 0,
    val userId: Int = 0,
    val pulseId: Int = 0,
    val questionText: String = "",
    val matchFormat: String, // Added for match format
    val matchTitle: String,  // Added for team names
    val price: BigDecimal = BigDecimal.ZERO,
    val orderSide: OrderSide = OrderSide.UNKNOWN,
    val orderType: OrderType = OrderType.UNKNOWN,
    val quantity: Long = 0,
    val remainingQuantity: Long = 0,
    val status: OrderStatus = OrderStatus.OPEN,
    val category: List<String> = emptyList(),
    val pulseEndDate: Instant? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    // Additional UI fields
    val isPulseActive: Boolean = true,
    val pulseImageUrl: String? = null,
    val totalTraders: Long = 0
)

// Extension function to convert Order to UiOrderResponse
fun Order.toUiOrderResponse(
): UiOrderResponse {
    return UiOrderResponse(
        id = id,
        matchId = match.id,
        userId = userId,
        pulseId = pulse.id,
        questionText = pulse.pulseQuestion,
        matchFormat = match.matchFormat ?: "T20",
        matchTitle = match.matchTitle,
        price = price,
        orderSide = orderSide,
        quantity = quantity,
        remainingQuantity = remainingQuantity,
        status = status,
        category = pulse.category?.split(",") ?: emptyList(),
        pulseEndDate = pulse.pulseEndDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isPulseActive = pulse.status == QuestionStatus.LIVE,
        pulseImageUrl = pulse.pulseImageUrl,
        totalTraders = (pulse.userACount ?: 0) + (pulse.userBCount ?: 0)
    )
}
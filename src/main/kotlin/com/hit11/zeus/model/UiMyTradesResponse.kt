package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
class UiMyTradesResponse(
    val matchId: Int = 0,
    val userId: Int = 0,
    val pulseId: Int = 0,
    val questionText: String = "",
    val matchFormat: String, // Added for match format
    val matchTitle: String,  // Added for team names
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

// combines user response + pulse data together
fun Trade.toUiMyTradesResponse(
    question: QuestionDataModel,
    match: Match
): UiMyTradesResponse {
    return UiMyTradesResponse(
        matchId = matchId,
        userId = userId,
        pulseId = pulseId,
        questionText = question.pulseQuestion,
        matchFormat = match.matchFormat ?: "T20",
        matchTitle = match.matchTitle,
        price = price,
        userAnswer = side.name,
        answerTime = createdAt,
        tradeResult = checkIfUserWon(side, question.pulseResult),
        isPulseActive = (question.status == QuestionStatus.LIVE),
        pulseImageUrl = question.pulseImageUrl,
        pulseEndDate = question.pulseEndDate,
        userTradeQuantity = quantity,
        totalTraders = question.userACount ?: (question.userBCount ?: 0)
    )
}
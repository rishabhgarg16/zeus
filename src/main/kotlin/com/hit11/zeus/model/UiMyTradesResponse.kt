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
    questionDataModel: QuestionDataModel
): UiMyTradesResponse {
    return UiMyTradesResponse(
        matchId = matchId,
        userId = userId,
        pulseId = pulseId,
        questionText = questionDataModel.pulseQuestion,
        price = price,
        userAnswer = side.name,
        answerTime = createdAt,
        tradeResult = checkIfUserWon(side, questionDataModel.pulseResult),
        isPulseActive = (questionDataModel.status == QuestionStatus.LIVE),
        pulseImageUrl = questionDataModel.pulseImageUrl,
        pulseEndDate = questionDataModel.pulseEndDate,
        userTradeQuantity = quantity,
        totalTraders = questionDataModel.userACount ?: (questionDataModel.userBCount ?: 0)
    )
}
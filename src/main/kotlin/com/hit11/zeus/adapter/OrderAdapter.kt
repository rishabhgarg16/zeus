package com.hit11.zeus.adapter

import com.hit11.zeus.model.*
import java.time.Instant

object OrderAdapter {

    fun convertToDataModel(request: OrderPlaceRequest): OrderDataModel {
        return OrderDataModel(
            userId = request.userId,
            pulseId = request.pulseId,
            matchId = request.matchId,
            userAnswer = request.userAnswer,
            answerTime = Instant.ofEpochMilli(request.answerTime),
            userWager = request.userWager,
            quantity = request.userTradeQuantity,
            tradeAmount = request.userTradeQuantity * request.userWager
        )
    }

    // combines user response + pulse data together
    fun OrderDataModel.toTradeResponse(questionDataModel: QuestionDataModel): TradeResponse {
        return TradeResponse(
            userId = userId,
            pulseId = pulseId,
            pulseDetail = questionDataModel.pulseQuestion,
            userWager = userWager,
            userAnswer = userAnswer,
            answerTime = answerTime,
            matchId = questionDataModel.matchId,
            userResult = checkIfUserWon(userAnswer, questionDataModel),
            isPulseActive = questionDataModel.enabled,
            pulseImageUrl = questionDataModel.pulseImageUrl,
            pulseEndDate = questionDataModel.pulseEndDate,
            userTradeQuantity = quantity,
            totalTraders = questionDataModel.userACount ?: (questionDataModel.userBCount ?: 0)
        )
    }
}

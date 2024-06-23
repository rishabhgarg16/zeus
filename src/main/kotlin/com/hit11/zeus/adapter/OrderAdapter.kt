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
    fun OrderDataModel.toTradeResponse(pulseDataModel: PulseDataModel): TradeResponse {
        return TradeResponse(
            userId = userId,
            pulseId = pulseId,
            pulseDetail = pulseDataModel.pulseQuestion,
            userWager = userWager,
            userAnswer = userAnswer,
            answerTime = answerTime,
            matchId = pulseDataModel.matchId,
            userResult = checkIfUserWon(userAnswer, pulseDataModel),
            isPulseActive = pulseDataModel.enabled,
            pulseImageUrl = pulseDataModel.pulseImageUrl,
            pulseEndDate = pulseDataModel.pulseEndDate,
            userTradeQuantity = quantity,
        )
    }
}

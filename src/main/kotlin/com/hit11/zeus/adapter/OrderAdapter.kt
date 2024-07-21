package com.hit11.zeus.adapter

import com.hit11.zeus.oms.OrderDataModel
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.oms.Trade
import com.hit11.zeus.oms.OrderPlaceRequest
import com.hit11.zeus.model.response.OrderResponse
import java.math.RoundingMode
import java.time.Instant

object OrderAdapter {

    fun convertToDataModel(request: OrderPlaceRequest): OrderDataModel {
        return OrderDataModel(
            userId = request.userId,
            pulseId = request.pulseId,
            matchId = request.matchId,
            userAnswer = request.userAnswer,
            answerTime = Instant.ofEpochMilli(request.answerTime),
            price = request.price.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP),
            quantity = request.userTradeQuantity,
            totalAmount = request.userTradeQuantity.toBigDecimal() * request.price.toBigDecimal()
                .setScale(2, RoundingMode.HALF_UP)
        )
    }

    // combines user response + pulse data together
    fun OrderDataModel.toOrderResponse(questionDataModel: QuestionDataModel): OrderResponse {
        return OrderResponse(
            userId = userId,
            pulseId = pulseId,
            pulseDetail = questionDataModel.pulseQuestion,
            price = price,
            userAnswer = userAnswer,
            answerTime = answerTime,
            matchId = questionDataModel.matchId,
            state = state.name,
            isPulseActive = questionDataModel.enabled,
            pulseImageUrl = questionDataModel.pulseImageUrl,
            pulseEndDate = questionDataModel.pulseEndDate,
            userTradeQuantity = quantity,
            totalTraders = questionDataModel.userACount ?: (questionDataModel.userBCount ?: 0)
        )
    }

    fun Trade.toOrderResponse(questionDataModel: QuestionDataModel): OrderResponse {
        return OrderResponse(
            userId = userId,
            pulseId = pulseId,
            pulseDetail = questionDataModel.pulseQuestion,
            price = tradePrice,
            userAnswer = userAnswer,
            answerTime = tradeTime,
            matchId = questionDataModel.matchId,
            state = checkIfUserWon(userAnswer, questionDataModel),
            isPulseActive = questionDataModel.enabled,
            pulseImageUrl = questionDataModel.pulseImageUrl,
            pulseEndDate = questionDataModel.pulseEndDate,
            userTradeQuantity = tradeQuantity,
            totalTraders = questionDataModel.userACount ?: (questionDataModel.userBCount ?: 0)
        )
    }
}

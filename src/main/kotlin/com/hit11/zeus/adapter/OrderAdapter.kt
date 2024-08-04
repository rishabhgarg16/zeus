package com.hit11.zeus.adapter

import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionStatus
import com.hit11.zeus.model.response.OrderResponse
import com.hit11.zeus.oms.OrderDataModel
import com.hit11.zeus.oms.OrderPlaceRequest
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
            // TODO change it may be not sure to send complete status to UI or partial
            isPulseActive = questionDataModel.status == QuestionStatus.LIVE,
            pulseImageUrl = questionDataModel.pulseImageUrl,
            pulseEndDate = questionDataModel.pulseEndDate,
            userTradeQuantity = quantity,
            totalTraders = questionDataModel.userACount ?: (questionDataModel.userBCount ?: 0)
        )
    }
}

package com.hit11.zeus.adapter

import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionStatus
import com.hit11.zeus.model.response.OrderResponse
import com.hit11.zeus.oms.OrderDataModel
import com.hit11.zeus.oms.OrderPlaceRequest
import com.hit11.zeus.utils.Constants
import java.time.Instant
import java.time.format.DateTimeParseException

object OrderAdapter {

    fun convertToDataModel(request: OrderPlaceRequest): OrderDataModel {
        return OrderDataModel(
            userId = request.userId,
            pulseId = request.pulseId,
            matchId = request.matchId,
            userAnswer = request.userAnswer,
            answerTime = try {
                Instant.parse(request.answerTime)
            } catch (e: DateTimeParseException) {
                throw OrderValidationException("Invalid answer time format")
            },
            price = request.price.toBigDecimal()
                .setScale(Constants.DEFAULT_SCALE, Constants.ROUNDING_MODE),
            quantity = request.userTradeQuantity,
            totalAmount = request.userTradeQuantity.toBigDecimal() * request.price.toBigDecimal()
                .setScale(Constants.DEFAULT_SCALE, Constants.ROUNDING_MODE),
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

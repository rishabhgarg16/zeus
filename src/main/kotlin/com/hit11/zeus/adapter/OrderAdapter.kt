package com.hit11.zeus.adapter

import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.model.response.OrderResponse
import com.hit11.zeus.utils.Constants
import java.time.Instant
import java.time.format.DateTimeParseException

object OrderAdapter {

//    fun convertToDataModel(request: OrderRequest): Order {
//        return Order(
//            userId = request.userId,
//            pulseId = request.pulseId,
//            matchId = request.matchId,
//                orderSide = OrderSide.fromString(request.userAnswer),
//            timestamp = try {
//                Instant.parse(request.answerTime)
//            } catch (e: DateTimeParseException) {
//                throw OrderValidationException("Invalid answer time format")
//            },
//            price = request.price.toBigDecimal()
//                .setScale(Constants.DEFAULT_SCALE, Constants.ROUNDING_MODE),
//            quantity = request.userTradeQuantity
//        )
//    }

    // combines user response + pulse data together
//    fun Order.toOrderResponse(questionDataModel: QuestionDataModel): OrderResponse {
//        return OrderResponse(
//            userId = userId,
//            pulseId = pulseId,
//            pulseDetail = questionDataModel.pulseQuestion,
//            price = price,
//            userAnswer = userAnswer,
//            answerTime = timestamp,
//            matchId = questionDataModel.matchId,
//            state = status.name,
//            // TODO change it may be not sure to send complete status to UI or partial
//            isPulseActive = questionDataModel.status == QuestionStatus.LIVE,
//            pulseImageUrl = questionDataModel.pulseImageUrl,
//            pulseEndDate = questionDataModel.pulseEndDate,
//            userTradeQuantity = quantity,
//            totalTraders = questionDataModel.userACount ?: (questionDataModel.userBCount ?: 0)
//        )
//    }
}

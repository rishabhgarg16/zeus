package com.hit11.zeus.adapter

import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.oms.Trade
import com.hit11.zeus.oms.TradeResponse

object TradeAdapter {
    fun Trade.toTradeResponse(questionDataModel: QuestionDataModel): TradeResponse {
        return TradeResponse(
            userId = userId,
            pulseId = pulseId,
            pulseDetail = questionDataModel.pulseQuestion,
            price = tradePrice,
            userAnswer = userAnswer,
            answerTime = tradeTime,
            matchId = questionDataModel.matchId,
            tradeResult = checkIfUserWon(userAnswer, questionDataModel),
            isPulseActive = questionDataModel.enabled,
            pulseImageUrl = questionDataModel.pulseImageUrl,
            pulseEndDate = questionDataModel.pulseEndDate,
            userTradeQuantity = tradeQuantity,
            totalTraders = questionDataModel.userACount ?: (questionDataModel.userBCount ?: 0)
        )
    }
}
package com.hit11.zeus.adapter

//object TradeAdapter {
//    fun Trade.toTradeResponse(questionDataModel: QuestionDataModel): TradeResponse {
//        return TradeResponse(
//            userId = userId,
//            pulseId = pulseId,
//            pulseDetail = questionDataModel.pulseQuestion,
//            price = price,
//            userAnswer = userAnswer,
//            answerTime = tradeTime,
//            matchId = questionDataModel.matchId,
//            tradeResult = checkIfUserWon(userAnswer, questionDataModel),
//            // TODO may be change it on
//            isPulseActive = questionDataModel.status == QuestionStatus.LIVE,
//            pulseImageUrl = questionDataModel.pulseImageUrl,
//            pulseEndDate = questionDataModel.pulseEndDate,
//            userTradeQuantity = quantity,
//            totalTraders = questionDataModel.userACount ?: (questionDataModel.userBCount ?: 0)
//        )
//    }
//}
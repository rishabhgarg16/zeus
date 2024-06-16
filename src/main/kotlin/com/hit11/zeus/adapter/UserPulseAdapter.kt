package com.hit11.zeus.adapter

import com.hit11.zeus.model.*
import java.time.Instant

object UserPulseAdapter {
//    fun toDataModel(request: UserPulseSubmissionRequest): UserPulseDataModel {
//        return UserPulseDataModel(
//            userId = request.userId,
//            pulseId = request.pulseId,
//            matchId = request.matchId,
//            userAnswer = request.userAnswer,
//            answerTime = Instant.ofEpochMilli(request.answerTime),
//            userWager = request.userWager,
//            userResult = request.userResult
//        )
//    }

    fun toDataModelNew(request: UserTradeSubmissionRequest): UserPulseDataModel {
        return UserPulseDataModel(
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
    fun UserPulseDataModel.toResponse(pulseDataModel: PulseDataModel): UserPulseSubmissionResponse {
        return UserPulseSubmissionResponse(
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
            userACount = pulseDataModel.userACount,
            userBCount = pulseDataModel.userBCount,
            pulseEndDate = pulseDataModel.pulseEndDate
        )
    }
}

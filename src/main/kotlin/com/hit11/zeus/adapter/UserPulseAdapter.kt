package com.hit11.zeus.adapter

import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.UserPulseDataModel
import com.hit11.zeus.model.UserPulseSubmissionRequest
import com.hit11.zeus.model.UserPulseSubmissionResponse

object UserPulseAdapter {
    fun toDataModel(request: UserPulseSubmissionRequest): UserPulseDataModel {
        return UserPulseDataModel(
            userId = request.userId,
            pulseId = request.pulseId,
            matchIdRefString = request.matchIdRef,
            userAnswer = request.userAnswer,
            answerTime = System.currentTimeMillis() / 1000,
            userWager = request.userWager,
            userResult = request.userResult
        )
    }


    // combines user response + pulse data together
    fun UserPulseDataModel.toResponse(pulseDataModel: PulseDataModel): UserPulseSubmissionResponse {
        return UserPulseSubmissionResponse(
            userId = userId,
            pulseId = pulseId,
            pulseDetail = pulseDataModel.pulseDetails,
            pulseText = pulseDataModel.pulseText,
            userWager = userWager,
            userAnswer = userAnswer,
            answerTime = answerTime,
            matchIdRef = pulseDataModel.matchIdRef!!.path,
            userResult = checkIfUserWon(userAnswer, pulseDataModel),
            isPulseActive = pulseDataModel.enabled,
            pulseImageUrl = pulseDataModel.pulseImageUrl,
            userACount = pulseDataModel.userACount,
            userBCount = pulseDataModel.userBCount,
            pulseEndDate = pulseDataModel.pulseEndDate
        )
    }
}

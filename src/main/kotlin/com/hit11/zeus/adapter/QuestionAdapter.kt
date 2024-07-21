package com.hit11.zeus.adapter

import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.response.QuestionResponse
import java.time.Instant

fun QuestionDataModel.toQuestionResponse(): QuestionResponse {
    return QuestionResponse(
        id = id,
        matchId = matchId,
        pulseDetails = pulseQuestion,
        optionA = optionA,
        optionAWager = optionAWager,
        optionB = optionB,
        optionBWager = optionBWager,
        userACount = userACount ?: 100,
        userBCount = userBCount ?: 100,
        category = category ?: emptyList(),
        enabled = enabled,
        pulseImageUrl = pulseImageUrl ?: "",
        pulseEndDate = pulseEndDate ?: Instant.now()
    )
}
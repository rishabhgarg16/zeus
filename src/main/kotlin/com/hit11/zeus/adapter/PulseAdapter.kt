package com.hit11.zeus.adapter

import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.PulseDataModelResponse
import java.time.Instant

fun PulseDataModel.addPulseData(): PulseDataModelResponse {
    return PulseDataModelResponse(
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
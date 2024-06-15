package com.hit11.zeus.adapter

import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.PulseDataModelResponse

fun PulseDataModel.toResponse(): PulseDataModelResponse {
    return PulseDataModelResponse(
        id = id,
        docRef = docRef,
        matchIdRef = matchIdRef?.path,
        pulseDetails = pulseDetails,
        pulseText = pulseText,
        optionA = optionA,
        optionAWager = optionAWager,
        optionB = optionB,
        optionBWager = optionBWager,
        userACount = userACount,
        userBCount = userBCount,
        category = category,
        enabled = enabled,
        tradersInterested = tradersInterested,
        pulseImageUrl = pulseImageUrl,
        pulseEndDate = pulseEndDate
    )
}
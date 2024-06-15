package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class PulseDataModelResponse(
    var id: Int = 0,
    var docRef: String = "",
    var matchIdRef: String? = null,
    var pulseDetails: String = "",
    var pulseText: String = "",
    var optionA: String = "",
    var optionAWager: Long = -1L,
    var optionB: String = "",
    var optionBWager: Long = -1L,
    var userACount: Long = -1L,
    var userBCount: Long = -1L,
    var category: List<String> = ArrayList(),
    var enabled: Boolean = false,
    var tradersInterested: Long = -1L,
    var pulseImageUrl: String = "",
    var pulseEndDate: Long = -1L
)
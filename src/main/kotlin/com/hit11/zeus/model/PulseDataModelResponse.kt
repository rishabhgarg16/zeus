package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.Instant

class PulseDataModelResponse(
    var id: Int = 0,
    val matchId: Int = 0,
    var pulseDetails: String = "",
    var optionA: String = "",
    var optionAWager: Long = -1L,
    var optionB: String = "",
    var optionBWager: Long = -1L,
    var userACount: Long = -1L,
    var userBCount: Long = -1L,
    var category: List<String> = ArrayList(),
    var enabled: Boolean = false,
    var pulseImageUrl: String = "",
    var pulseEndDate: Instant = Instant.now(),
)
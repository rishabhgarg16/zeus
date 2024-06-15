package com.hit11.zeus.model

class PulseAnswerUpdateResponse(
    var updatedUserIds: List<String> = emptyList<String>(),
    var totalWon: Long = -1L,
    var totalLost: Long = -1L,
    var totalActive: Long = -1L,
    var status: String = "",
)
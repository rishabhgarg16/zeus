package com.hit11.zeus.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hit11.zeus.config.PulseDataModelDeserializer

enum class UnitOption(val optionText: String) {
    YES("Yes"),
    NO("No")
}

enum class PulseOutcome(val outcome: Int) {
    WON(1),
    LOSE(2),
    ACTIVE(3),
}

data class Option (
    val optionUnit: UnitOption,
    val wager: Double,
    val traderCount: Long
)

@JsonDeserialize(using = PulseDataModelDeserializer::class)
class PulseDataModel(
    val id: Int = 0,
    val pulseDetail: String = "",
    val pulseText: String = "",
    val options: List<Option> = emptyList(),
    val category: List<String> = ArrayList(),
    val tradersInterested: Long = -1L,
    val enabled: Boolean = false,
    val pulseOutcome: PulseOutcome = PulseOutcome.ACTIVE,
)

class UserPulseDataModel(
    val userId: Int = -1,
    val pulseId: Int = -1,
    val answerChosen: Option? = null,
    val answerTime: Long = -1L,
    val matchId: Int = -1,
)

class UserPulseSubmissionResponse(
    val userId: Int = -1,
    val pulseId: Int = -1,
    val pulseDetail: String = "",
    val pulseText: String = "",
    val userAnswer: Option? = null,
    val pulseResult: PulseOutcome = PulseOutcome.ACTIVE,
    val answerTime: Long = -1L,
    val matchId: Long = -1L,
)

class UserPulseSubmissionRequest(
    val userId: Int = -1,
    val pulseId: Int = -1,
    val answerChosen: Option? = null,
    val answerTime: Long = -1L,
    val matchId: Int = -1,
)


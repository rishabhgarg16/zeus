package com.hit11.zeus.model

import com.google.cloud.firestore.DocumentReference
import com.google.firebase.database.Exclude

enum class Option(val optionText: String) {
    YES("Yes"),
    NO("No")
}

enum class PulseOutcome(val outcome: Int) {
    WON(1),
    LOSE(2),
    ACTIVE(3),
}

//data class Option (
//    val optionUnit: UnitOption,
//    val wager: Double,
//    val traderCount: Long
//)

//@JsonDeserialize(using = PulseDataModelDeserializer::class)
class PulseDataModel(
    var id: Int = 0,
    var docRef: String = "",
    @Exclude
    var matchIdRef: DocumentReference? = null,
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
    var tradersInterested: Long = -1L
)

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
        tradersInterested = tradersInterested
    )
}
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
    var tradersInterested: Long = -1L
)

class UserPulseDataModel(
    val userId: String = "",
    val pulseId: String = "",
    val answerChosen: String = "",
    val answerTime: Long = -1L,
    val userWager: Double = -1.0,
    val matchId: String = "",
)

class UserPulseSubmissionResponse(
    val userId: Int = -1,
    val pulseId: Int = -1,
    val pulseDetail: String = "",
    val pulseText: String = "",
    val userAnswer: Option? = null,
    val pulseResult: PulseOutcome = PulseOutcome.ACTIVE,
    val answerTime: Long = -1L,
    val matchIdRef: String = "",
)

class UserPulseSubmissionRequest(
    val userId: String = "",
    val pulseIdRef: String = "",
    val userAnswer: String = "",
    val answerTime: Long = -1L,
    val userWager: Double = -1.0,
    val matchIdRef: String = "",
)


package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.cloud.firestore.DocumentReference
import com.google.firebase.database.Exclude
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class Option(val optionText: String) {
    YES("Yes"),
    NO("No")
}

enum class UserResult(val text: String, val outcome: Int) {
    WIN("Win", 1),
    LOSE("Lose", 2),
    ACTIVE("Active", 3),;

    companion object {
        fun fromText(userResult: String): UserResult {
            when(userResult) {
                "Yes" -> return WIN
                "No" -> return LOSE
                "Active" -> return ACTIVE
            }
            return ACTIVE
        }
    }
}

@Serializable
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
    @Transient var tradersInterested: Long = -1L,
    var pulseResult: String = ""
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

@Serializable
data class UserPulseDataModel(
    val userId: String = "",
    val pulseId: String = "",
    @SerializedName("matchIdRef") val matchIdRefString: String = "",
    @Transient val matchIdRef: DocumentReference? = null,
    val userAnswer: String = "",
    val answerTime: Long = -1L,
    val userWager: Double = -1.0,
    var userResult: String = UserResult.ACTIVE.text,

    ) {
    fun checkIfUserWon(userAnswer: String, pulseDataModel: PulseDataModel): String {
        return when {
            pulseDataModel.enabled -> UserResult.ACTIVE.text
            pulseDataModel.pulseResult.isEmpty() -> UserResult.ACTIVE.text
            userAnswer == pulseDataModel.pulseResult-> UserResult.WIN.text
            else -> UserResult.LOSE.text
        }
    }
}

class UserPulseSubmissionResponse(
    val userId: String = "",
    val matchIdRef: String = "",
    val pulseId: String = "",
    val pulseDetail: String = "",
    val pulseText: String = "",
    val userWager: Double = -1.0,
    val userAnswer: String = "",
    val answerTime: Long = -1L,
    val userResult: String = "",
    val isPulseActive: Boolean = true,
)

@JsonIgnoreProperties(ignoreUnknown = true)
class UserPulseSubmissionRequest(
    val userId: String = "",
    val pulseId: String = "",
    val matchIdRef: String = "",
    val userAnswer: String = "",
    val answerTime: Long = -1L,
    val userWager: Double = -1.0,
    val userResult: String = "",
    val isPulseActive: Boolean=false,
    val test: Boolean = false,
)
class GetUserEnrolledPulseRequest(
    val userId: String = "",
    val matchIdRef: String = "",
)

class PulseAnswerUpdateRequest(
    var pulseId: String = "",
    var pulseResult: String = "",
)

class PulseAnswerUpdateResponse(
    var updatedUserIds: List<String> = emptyList<String>(),
    var totalWon: Long = -1L,
    var totalLost: Long = -1L,
    var totalActive: Long = -1L,
    var status: String = "",
)


package com.hit11.zeus.model

import com.google.cloud.firestore.DocumentReference
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
            userAnswer == pulseDataModel.pulseResult -> UserResult.WIN.text
            else -> UserResult.LOSE.text
        }
    }
}
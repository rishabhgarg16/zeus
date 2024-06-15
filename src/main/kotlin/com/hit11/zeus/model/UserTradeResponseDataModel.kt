package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.cloud.firestore.DocumentReference
import com.google.firebase.database.Exclude
import java.time.Instant

// Maps 101 to userTradeResponse collection in firebase
@JsonIgnoreProperties(ignoreUnknown = true)
class UserTradeResponseDataModel(
    var docRef: String = "",
    @Exclude
    var matchIdRef: DocumentReference? = null,
    var pulseIdRef: DocumentReference? = null,
    var userIdRef: DocumentReference? = null,
    var userAnswer: String = "",
    var userWager: Double = 10.0,
    var userTradeQuantity: Long = 0L,
    var tradeAmount: Double = 0.0,
    var status: String = "",
    var answerTime: Instant? = null,
)
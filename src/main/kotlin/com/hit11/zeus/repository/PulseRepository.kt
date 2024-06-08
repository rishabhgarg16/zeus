package com.hit11.zeus.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.UserPulseDataModel
import com.hit11.zeus.model.UserTradeResponseDataModel
import com.hit11.zeus.model.UserTradeSubmissionRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.concurrent.ExecutionException


@Repository
class PulseRepository(@Autowired private val objectMapper: ObjectMapper) {
    private val firestore: Firestore = FirestoreClient.getFirestore()
    val pulseCollection = firestore.collection("pulse")
    var opinions = mutableListOf<PulseDataModel>()
    var lastUpdated: Instant = Instant.now()

    fun getAllActivePulseByMatch(matchId: String): List<PulseDataModel>? {
        if (Instant.now().isAfter(lastUpdated.plusSeconds(60)) and opinions.isNotEmpty()) {
            return opinions
        }

        val tempOpinions = mutableListOf<PulseDataModel>()
        lastUpdated = Instant.now()
        try {
            val matchIdRef: DocumentReference = firestore.document(matchId)
            val querySnapshot =
                pulseCollection
                    .whereEqualTo("enabled", true)
                    .whereEqualTo("matchIdRef", matchIdRef)
                    .limit(50)
                    .get()
                    .get()

            querySnapshot.map {
                var pulse = it.toObject(PulseDataModel::class.java)
                pulse.docRef = it.id
                tempOpinions.add(pulse)
            }
            opinions = tempOpinions
            return opinions
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
        }
        return null
    }


    fun findById(id: String): PulseDataModel? {
        return try {
            val documentSnapshot = pulseCollection.document(id).get().get()
            documentSnapshot.toObject(PulseDataModel::class.java)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        }
    }

    fun saveUserResponse(response: UserPulseDataModel): UserPulseDataModel? {
        try {
            val matchIdRef: DocumentReference = firestore.document(response.matchIdRefString)
            val userResponseCollection = firestore.collection("user_pulse_response")
            val query = userResponseCollection
                .whereEqualTo("userId", response.userId)
                .whereEqualTo("pulseId", response.pulseId)
                .whereEqualTo("matchId", matchIdRef)
                .get()
                .get()

            if (query.isEmpty) {
                val newDocRef = userResponseCollection.document()
                val responseWithRef = response.copy(matchIdRef = matchIdRef)
                newDocRef.set(responseWithRef).get()
                return responseWithRef
            } else {
                return UserPulseDataModel()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        }
        return null
    }

    fun getEnrolledPulsesByUser(userId: String): List<UserPulseDataModel>? {
        return try {
            val documentSnapshot =
                firestore.collection("user_pulse_response")
                    .whereEqualTo("userId", userId)
                    .orderBy("answerTime", Query.Direction.DESCENDING)
                    .get().get()
            return documentSnapshot.map { it.toObject(UserPulseDataModel::class.java) }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun getEnrolledPulsesByUserAndMatch(
        userId: String,
        matchIdRef: String
    ): List<UserPulseDataModel>? {
        return try {
            val matchIdRef: DocumentReference = firestore.document(matchIdRef)
            val documentSnapshot =
                firestore.collection("user_pulse_response")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("matchIdRef", matchIdRef)
                    .orderBy("answerTime", Query.Direction.DESCENDING)
                    .get().get()
            return documentSnapshot.toObjects(UserPulseDataModel::class.java)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        }
    }

    fun updatePulseAnswer(pulseId: String, answer: String): Boolean {
        try {
            val documentSnapshot = pulseCollection.document(pulseId).get().get()
            val pulse = documentSnapshot.toObject(PulseDataModel::class.java)
            if (pulse?.optionA == answer || pulse?.optionB == answer) {
                pulse.enabled = false
                pulse.pulseResult = answer
            } else {
                throw Exception("Invalid answer")
            }
            pulseCollection.document(pulseId).set(pulse).get()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun getPulseById(pulseId: String?): PulseDataModel {
        if (pulseId == null) return PulseDataModel()
        var finalPulseId = pulseId
        if (pulseId.contains("pulse")) {
            finalPulseId = pulseId.split("/", limit = 2)[1]
        }
        val documentSnapshot = firestore.collection("pulse").document(finalPulseId).get().get()
        return documentSnapshot.toObject(PulseDataModel::class.java)!!
    }

    fun saveUserTrade(req: UserTradeSubmissionRequest): Boolean {
        try {
            var amount = req.userWager * req.userTradeQuantity
            val userIdRef = firestore.document(req.userIdRef)
            val pulseIdRef = firestore.document(req.pulseIdRef)
            val pulseSnapshot = pulseIdRef.get().get()
            val matchIdRef = pulseSnapshot.get("matchIdRef") as DocumentReference
            val userTrade = UserTradeResponseDataModel()
            userTrade.matchIdRef = matchIdRef
            userTrade.userIdRef = userIdRef
            userTrade.pulseIdRef = pulseIdRef
            userTrade.userAnswer = req.userAnswer
            userTrade.userWager = req.userWager
            userTrade.userTradeQuantity = req.userTradeQuantity
            val userTradeRef = firestore.collection("user_trade_response").document()
            userTradeRef.set(userTrade).get()
            return true
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }
}

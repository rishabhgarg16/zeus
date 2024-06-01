package com.hit11.zeus.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.UserPulseDataModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.concurrent.ExecutionException


@Repository
class PulseRepository(@Autowired private val objectMapper: ObjectMapper) {
    private val firestore: Firestore = FirestoreClient.getFirestore()
    val quizzesRef = firestore.collection("pulse")

    fun getAllActivePulseByMatch(matchId: String): List<PulseDataModel>? {
        var opinions = mutableListOf<PulseDataModel>()
        try {
            val matchIdRef : DocumentReference = firestore.document(matchId)
            val querySnapshot =
                quizzesRef
                    .whereEqualTo("enabled", true)
                    .whereEqualTo("matchIdRef", matchIdRef)
                    .get()
                    .get()
            querySnapshot.map {
                var pulse = it.toObject(PulseDataModel::class.java)
                pulse.docRef = it.id
                opinions.add(pulse)
            }
            return opinions
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
        }
        return null
    }


    fun findById(id: String): PulseDataModel? {
        return try {
            val documentSnapshot = quizzesRef.document(id).get().get()
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
            val matchIdRef : DocumentReference = firestore.document(response.matchIdRefString)
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
            return documentSnapshot.toObjects(UserPulseDataModel::class.java)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        }
    }

    fun getEnrolledPulsesByUserAndMatch(userId: String, matchIdRef: String): List<UserPulseDataModel>? {
        return try {
            val matchIdRef : DocumentReference = firestore.document(matchIdRef)
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

    fun getPulseById(pulseId: String?): PulseDataModel {
        if (pulseId == null) return PulseDataModel()
        val documentSnapshot = firestore.collection("pulse").document(pulseId).get().get()
        return documentSnapshot.toObject(PulseDataModel::class.java)!!
    }
}

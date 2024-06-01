package com.hit11.zeus.repository

import com.fasterxml.jackson.databind.ObjectMapper
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
            val querySnapshot =
                quizzesRef
                    .whereEqualTo("enabled", true)
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
            val userResponseCollection = firestore.collection("user_pulse_response")
            val query = userResponseCollection
                .whereEqualTo("userId", response.userId)
                .whereEqualTo("pulseId", response.pulseId)
                .get()
                .get()

            if (query.isEmpty) {
                val newDocRef = userResponseCollection.document()
                newDocRef.set(response).get()
                return response
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

    fun getEnrolledPulsesByUser(userId: Int): List<UserPulseDataModel>? {
        return try {
            var userAnswers = mutableListOf<UserPulseDataModel>()
            val documentSnapshot =
                firestore.collection("user_pulse_response").whereEqualTo("user_id", userId)
                    .orderBy("answer_time", Query.Direction.DESCENDING).get().get()
            documentSnapshot.map {
                if (it.exists()) {
                    val opinion = objectMapper.convertValue(it.data, UserPulseDataModel::class.java)
                    userAnswers.add(opinion)
                }
            }
            return userAnswers
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        }
    }

    fun getEnrolledPulsesByUserAndMatch(userId: Int, matchId: Int): List<UserPulseDataModel>? {
        return try {
            var userAnswers = mutableListOf<UserPulseDataModel>()
            val documentSnapshot =
                firestore.collection("user_pulse_response").whereEqualTo("user_id", userId)
                    .whereEqualTo("match_id", matchId)
                    .orderBy("answer_time", Query.Direction.DESCENDING).get().get()
            documentSnapshot.map {
                if (it.exists()) {
                    val opinion = objectMapper.convertValue(it.data, UserPulseDataModel::class.java)
                    userAnswers.add(opinion)
                }
            }
            return userAnswers
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        }
    }


}

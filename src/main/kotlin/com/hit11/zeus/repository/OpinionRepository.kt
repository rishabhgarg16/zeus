package com.hit11.zeus.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.model.Match
import com.hit11.zeus.model.OpinionDataModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.util.concurrent.ExecutionException


@Repository
class OpinionRepository(@Autowired private val objectMapper: ObjectMapper) {
    private val firestore: Firestore = FirestoreClient.getFirestore()
    val quizzesRef = firestore.collection("quizzes")

    fun getAllActiveOpinionsByMatch(matchId: Int): List<OpinionDataModel>? {
        var opinions = mutableListOf<OpinionDataModel>()
        try {
            val querySnapshot =
                quizzesRef.whereEqualTo("match_id", matchId).whereEqualTo("enabled", true).get()
                    .get()
            querySnapshot.map { it ->
                if (it.exists()) {
                    val opinion = objectMapper.convertValue(it.data, OpinionDataModel::class.java)
                    opinions.add(opinion)
                }
            }
            return opinions
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
        }
        return null
    }


    fun findById(id: String): OpinionDataModel? {
        return try {
            val documentSnapshot = quizzesRef.document(id).get().get()
            documentSnapshot.toObject(OpinionDataModel::class.java)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            null
        } catch (e: ExecutionException) {
            e.printStackTrace()
            null
        }
    }


}

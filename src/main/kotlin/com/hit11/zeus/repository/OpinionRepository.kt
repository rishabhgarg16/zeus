package com.hit11.zeus.repository

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.obsidian.warhammer.repository.model.OpinionDataModel
import org.springframework.stereotype.Repository
import java.util.concurrent.ExecutionException


@Repository
class OpinionRepository() {
    private val firestore: Firestore = FirestoreClient.getFirestore()
    val quizzesRef = firestore.collection("quizzes")

    fun getAllActiveOpinionsByMatch(matchId: Int): List<OpinionDataModel>? {
        try {
            val querySnapshot = quizzesRef.whereEqualTo("matchId", matchId).whereEqualTo("enabled", true).get().get()
            return querySnapshot.toObjects(OpinionDataModel::class.java)
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

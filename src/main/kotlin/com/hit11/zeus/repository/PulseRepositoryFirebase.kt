package com.hit11.zeus.repository

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Repository


@Repository
class PulseRepositoryFirebase() {
    private val firestore: Firestore = FirestoreClient.getFirestore()
    val pulseCollection = firestore.collection("pulse")

//    fun updatePulseAnswer(pulseId: String, answer: String): Boolean {
//        try {
//            val documentSnapshot = pulseCollection.document(pulseId).get().get()
//            val pulse = documentSnapshot.toObject(PulseDataModel::class.java)
//            if (pulse?.optionA == answer || pulse?.optionB == answer) {
//                pulse.enabled = false
//                pulse.pulseResult = answer
//            } else {
//                throw Exception("Invalid answer")
//            }
//            pulseCollection.document(pulseId).set(pulse).get()
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//            return false
//        }
//        return true
//    }
}

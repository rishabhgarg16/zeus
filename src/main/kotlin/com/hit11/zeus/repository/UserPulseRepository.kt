package com.hit11.zeus.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.model.UserPulseDataModel
import com.hit11.zeus.model.UserResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class UserPulseRepository(@Autowired private val objectMapper: ObjectMapper) {
    private val firestore: Firestore = FirestoreClient.getFirestore()
    val userPulseCollection = firestore.collection("user_pulse_response")

//    fun updatePulseResultsForAllUsers(pulseId: String, answer: String): List<String> {
//        var updateWalletForUsers = emptyList<String>()
//        val docRefs = userPulseCollection.whereEqualTo("pulseId", pulseId).get().get()
//
//        for (docRef in docRefs) {
//            var userPulse = docRef.toObject(UserPulseDataModel::class.java)
//            if (true || (userPulse.userResult == UserResult.ACTIVE.text ||
//                        userPulse.userResult == "")) {
//                if (userPulse.userAnswer == answer) {
//                    userPulse.userResult = UserResult.WIN.text
//                } else {
//                    userPulse.userResult = UserResult.LOSE.text
//                }
//                updateWalletForUsers += userPulse.userId
//                docRef.reference.set(userPulse).get()
////                var writeResult = userPulseCollection.document(docRef.id).set(userPulse).get()
//            }
//        }
//        return updateWalletForUsers
//    }
}
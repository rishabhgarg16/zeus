package com.hit11.zeus.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.controller.PulseController
import com.hit11.zeus.model.UserPulseDataModel
import com.hit11.zeus.model.UserResult
import com.hit11.zeus.model.UserTradeResponseDataModel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class UserTradeRepository(@Autowired private val objectMapper: ObjectMapper) {
    private val logger = LoggerFactory.getLogger(UserTradeRepository::class.java)
    private val firestore: Firestore = FirestoreClient.getFirestore()
    val userPulseCollection = firestore.collection("user_trade_response")

    fun updatePulseTradeResultsForAllUsers(pulseId: String, answer: String): List<UserTradeResponseDataModel> {
        var responseList = emptyList<UserTradeResponseDataModel>()
//        var updateWalletForUsers = emptyList<String>()
        var pulseIdRef: DocumentReference
        if (!pulseId.contains("pulse")) {pulseIdRef = firestore.document("pulse/"+pulseId)}
        else {pulseIdRef = firestore.document(pulseId)}
        val docRefs = userPulseCollection.whereEqualTo("pulseIdRef", pulseIdRef).get().get()

        for (docRef in docRefs) {
            var userPulse = docRef.toObject(UserTradeResponseDataModel::class.java)
            if (true || (userPulse.userAnswer == UserResult.ACTIVE.text ||
                        userPulse.userAnswer == "")) {
                if (userPulse.userAnswer == answer) {
                    userPulse.status = UserResult.WIN.text
                } else {
                    userPulse.status = UserResult.LOSE.text
                }
                responseList = responseList.plus(userPulse)
//                try {
//                    updateWalletForUsers += userPulse.userIdRef!!.path
//
//                } catch (e: Exception) {
//                    logger.error("updatePulseTradeResultsForAllUsers: ", e)
//                }
                docRef.reference.set(userPulse).get()
//                var writeResult = userPulseCollection.document(docRef.id).set(userPulse).get()
            }
        }
        return responseList
    }
}
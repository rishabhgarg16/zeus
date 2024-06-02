package com.hit11.zeus.repository

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.exceptions.InsufficientFundsException
import com.hit11.zeus.model.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class UserRepository {
    private val logger = LoggerFactory.getLogger(UserRepository::class.java)
    private val firestore: Firestore = FirestoreClient.getFirestore()
    val userCollection = firestore.collection("users")
    fun getUser(firebaseUID: String): User? {
        return userCollection.document(firebaseUID).get().get().toObject(User::class.java)
    }

    // This function will create an entry in the users collection in Firestore
    // Actual user will still reside in FirebaseAuth
    fun createUser(user: User): User? {
        val userRef = userCollection.document(user.firebaseUID).get()
        if (!userRef.get().exists()) {
            val res = userCollection.document(user.firebaseUID).set(user).get()
            val createdUserRef = userCollection.document(user.firebaseUID).get().get()
            if (createdUserRef.exists()) {
                val createdUser = createdUserRef.toObject(User::class.java)
                return createdUser
            }
        }
        return null
    }

    fun updateBalance(firebaseUID: String, amount: Double): Boolean {
        var userRef = userCollection.document(firebaseUID)

        userRef.get().get().toObject(User::class.java)?.let {
            user ->
            val newBalance = user.walletBalance + amount
            if (newBalance < 0) {
                logger.error("Insufficient Funds ${firebaseUID}")
                throw InsufficientFundsException()
            }

            var success = userRef.update("walletBalance", newBalance)
            if (success.get() != null) {
                return true
            }
        }
        return false
    }
}
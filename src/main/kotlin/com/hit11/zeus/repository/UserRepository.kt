package com.hit11.zeus.repository

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.exception.InsufficientBalanceException
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
    fun createUser(firebaseUID: String): User? {
        val firebaseUser = FirebaseAuth.getInstance().getUser(firebaseUID)
        if (firebaseUser != null) {
            val userRef = userCollection.document(firebaseUID).get()
            if (!userRef.get().exists()) {
                val user = User(
                    0,
                    firebaseUser.uid,
                    firebaseUser.email,
                    firebaseUser.displayName,
                    firebaseUser.phoneNumber,
                    500.0,
                    0.0
                )
                val res = userCollection.document(firebaseUID).set(user).get()
                val createdUserRef = userCollection.document(user.firebaseUID).get().get()
                if (createdUserRef.exists()) {
                    val createdUser = createdUserRef.toObject(User::class.java)
                    return createdUser
                }
            }
        }
        return null
    }

    private  fun updateBalance(userRef: DocumentReference, amount: Double): Boolean {
        val snapshot = userRef.get().get()
        snapshot.toObject(User::class.java)?.let { user ->
            val newBalance = user.walletBalance + amount
            if (newBalance < 0) {
                logger.error("Insufficient Funds ${user.id}")
                throw InsufficientBalanceException("Insufficient Balance ${user.id}")
            }

            var success = userRef.update("walletBalance", newBalance)
            if (success.get() != null) {
                return true
            }
        }

        return false
    }

    fun updateBalanceForUserRef(userIdRef: Int, amount: Double): Boolean {
        var userIdRef = "users/EA1OWNYtBxQBHUwAQzaUqhS30mw2"
        val userRef = firestore.document(userIdRef)
        return updateBalance(userRef, amount)
    }
}
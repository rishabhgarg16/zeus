package com.hit11.zeus.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.firebase.auth.AuthErrorCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.model.User
import org.springframework.stereotype.Service


@Service
class UserService() {
    private val firestore: Firestore = FirestoreClient.getFirestore()
    private val userCollection = firestore.collection("users")

    fun getUserFromAuth(firebaseUID: String): UserRecord? {
        try {
            val userRecord = FirebaseAuth.getInstance().getUser(firebaseUID)
            return userRecord
        } catch (e: FirebaseAuthException) {
            if (e.authErrorCode == AuthErrorCode.USER_NOT_FOUND) {
                println("User not found")
                return null
            }
        }
        return null
    }

    fun getUser(firebaseUID: String): ApiFuture<DocumentSnapshot> {
        val userRef = userCollection.document(firebaseUID)
        val userSnapshot = userRef.get()
        return userSnapshot
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
}
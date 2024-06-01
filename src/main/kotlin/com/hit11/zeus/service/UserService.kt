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
import com.hit11.zeus.repository.UserRepository
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository
) {

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

    fun getUser(firebaseUID: String): User? {
        return userRepository.getUser(firebaseUID)
    }

    fun createUser(user: User): User? {
       return userRepository.createUser(user)
    }
}
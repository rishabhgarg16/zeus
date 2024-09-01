package com.hit11.zeus.service

import com.google.cloud.firestore.Firestore
import com.google.firebase.auth.AuthErrorCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.ZeusApplication
import com.hit11.zeus.exception.InsufficientBalanceException
import com.hit11.zeus.exception.UserNotFoundException
import com.hit11.zeus.model.User
import com.hit11.zeus.model.UserEntity
import com.hit11.zeus.model.UserReward
import com.hit11.zeus.model.mapToUser
import com.hit11.zeus.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.Date
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant
import java.util.concurrent.TimeUnit


@Service
class UserService(
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)
    private val firestore: Firestore = FirestoreClient.getFirestore()
    val userCollection = firestore.collection("users")

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
        val userRecord = userRepository.findByFirebaseUID(firebaseUID) ?: return null
        return mapToUser(userRecord)
    }

    fun createUser(firebaseUID: String): User? {
        val firebaseUser = FirebaseAuth.getInstance().getUser(firebaseUID)
        if (firebaseUser != null) {
            val newUser = UserEntity(
                0,
                firebaseUID = firebaseUser.uid,
                email = firebaseUser.email,
                firebaseUser.displayName,
                firebaseUser.phoneNumber,
                BigDecimal(500),
                BigDecimal.ZERO,
                lastLoginDate = Date(),
            )
            try {
                val createdUser = userRepository.save(newUser)
                return mapToUser(createdUser)
            } catch (e: SQLIntegrityConstraintViolationException) {
                throw Exception("User Already Exists")
            } catch (e: Exception) {
                throw Exception(e.message)
            }
        }
        return null
    }

    fun checkUserReward(firebaseUID: String): UserReward? {
        val userRecord = userRepository.findByFirebaseUID(firebaseUID) ?: return null
        if (userRecord.lastLoginDate != null) {
            val oldLoginDate = userRecord.lastLoginDate!!
            val newLoginDate = Date()
            val diffInMillis = newLoginDate.time - oldLoginDate.time
            val diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
            val newUserRecord = userRepository.save(userRecord.copy(lastLoginDate = newLoginDate))
            if (diffInDays > 0L) {
                val rewardAmount = 200.0
                if (this.updateBalance(userRecord.id, rewardAmount)) {
                    logger.info("Added $rewardAmount to user with ID: ${userRecord.id}")
                    return UserReward(
                        "DAILY_LOGIN_REWARD",
                        "NON_WITHDRAWAL_BALANCE",
                        rewardAmount,
                        Instant.now().epochSecond
                    )
                }
            }
        }
        return null
    }

    @Transactional
    fun updateBalance(userId: Int, amount: Double): Boolean {
        val user = userRepository.findById(userId).orElseThrow {
            throw UserNotFoundException("User not found with ID: $userId")
        }

        val newBalance = user.walletBalance.plus(amount.toBigDecimal().setScale(2))
        if (newBalance < BigDecimal.ZERO) {
            throw InsufficientBalanceException("Insufficient Balance for user with ID: $userId")
        }

        user.walletBalance = newBalance
        userRepository.save(user)
        return true
    }
}
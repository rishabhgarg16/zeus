package com.hit11.zeus.service

import com.google.firebase.auth.AuthErrorCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.hit11.zeus.exception.UserAlreadyExistsException
import com.hit11.zeus.exception.UserNotFoundException
import com.hit11.zeus.model.User
import com.hit11.zeus.model.UserReward
import com.hit11.zeus.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit


@Service
class UserService(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun getUserFromAuth(firebaseUID: String): UserRecord? {
        try {
            val userRecord = firebaseAuth.getUser(firebaseUID)
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
        return userRepository.findByFirebaseUID(firebaseUID)
    }

    fun createUser(firebaseUID: String, fcmToken: String): User {
        val firebaseUser = try {
            firebaseAuth.getUser(firebaseUID)
        } catch (e: FirebaseAuthException) {
            logger.error("Invalid Firebase user", e)
            throw UserNotFoundException("Firebase user not found")
        }

        val existingUser = userRepository.findByFirebaseUID(firebaseUID)

        return existingUser?.let { user ->
            // Update existing user's FCM token if different
            if (user.fcmToken != fcmToken) {
                user.fcmToken = fcmToken
                user.lastLoginDate = Date()
                logger.info("Updated FCM token for user: ${user.id}")
                userRepository.save(user)
            } else {
                user
            }
        } ?: run {
            val newUser = User(
                id = 0,
                firebaseUID = firebaseUser.uid,
                fcmToken = fcmToken,
                email = firebaseUser.email,
                name = firebaseUser.displayName,
                phone = firebaseUser.phoneNumber,
                walletBalance = BigDecimal(INITIAL_WALLET_BALANCE),
                reservedBalance = BigDecimal.ZERO,
                lastLoginDate = Date()
            )
            try {
                val savedUser = userRepository.save(newUser)
                logger.info("New user created: ${savedUser.id}")
                savedUser
            } catch (e: SQLIntegrityConstraintViolationException) {
                logger.error("User creation failed due to constraint violation", e)
                throw UserAlreadyExistsException("User already exists")
            } catch (e: Exception) {
                logger.error("Unexpected error during user creation", e)
                throw e
            }
        }
    }

    fun updateFCMToken(firebaseUID: String, fcmToken: String): Boolean {
        return try {
            // Find user
            val user = userRepository.findByFirebaseUID(firebaseUID)
                ?: throw UserNotFoundException("User not found")

            // Update FCM token
            user.fcmToken = fcmToken
            userRepository.save(user)

            true
        } catch (e: Exception) {
            logger.error("Error updating FCM token for user $firebaseUID", e)
            false
        }
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
                val rewardAmount = DAILY_REWARD_AMOUNT
                if (this.updateUserWallet(userRecord.id, rewardAmount.toBigDecimal())) {
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
    fun reserveBalance(userId: Int, amount: BigDecimal): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        if (user.availableBalance < amount) {
            return false
        }

        user.reservedBalance += amount
        userRepository.save(user)
        return true
    }

    @Transactional
    fun confirmReservedBalance(userId: Int, amount: BigDecimal) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        user.walletBalance -= amount
        user.reservedBalance -= amount
        userRepository.save(user)
    }

    @Transactional
    fun releaseReservedBalance(userId: Int, amount: BigDecimal) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        user.reservedBalance -= amount
        userRepository.save(user)
    }

    @Transactional
    fun updateUserWallet(userId: Int, amount: BigDecimal): Boolean {
        val user = userRepository.findById(userId).orElseThrow {
            throw UserNotFoundException("User not found with ID: $userId")
        }

        var newBalance = user.walletBalance.plus(amount.setScale(2))
        if (newBalance < BigDecimal.ZERO) {
            newBalance = BigDecimal.ZERO
        }

        user.walletBalance = newBalance
        userRepository.save(user)
        return true
    }

    companion object {
        private const val DAILY_REWARD_AMOUNT = 200.0
        private const val INITIAL_WALLET_BALANCE = 500.0
        private const val MINIMUM_DAYS_BETWEEN_REWARDS = 1L
    }
}
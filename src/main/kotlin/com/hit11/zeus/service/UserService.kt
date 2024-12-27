package com.hit11.zeus.service

import com.google.firebase.auth.AuthErrorCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
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
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

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
        return userRepository.findByFirebaseUID(firebaseUID)
    }

    fun createUser(firebaseUID: String, fcmToken: String): User {
        val firebaseUser = FirebaseAuth.getInstance().getUser(firebaseUID)
        val newUser = User(
            0,
            firebaseUID = firebaseUser.uid,
            fcmToken = fcmToken,
            email = firebaseUser.email,
            firebaseUser.displayName,
            firebaseUser.phoneNumber,
            BigDecimal(500),
            BigDecimal.ZERO,
            lastLoginDate = Date(),
        )
        try {
            return userRepository.save(newUser)
        } catch (e: SQLIntegrityConstraintViolationException) {
            throw Exception("User Already Exists")
        } catch (e: Exception) {
            throw Exception(e.message)
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
                val rewardAmount = 200.0
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
}
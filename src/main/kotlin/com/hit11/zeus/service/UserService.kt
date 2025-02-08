package com.hit11.zeus.service

import com.google.firebase.auth.AuthErrorCode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import com.hit11.zeus.exception.UserAlreadyExistsException
import com.hit11.zeus.exception.UserNotFoundException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.PromotionalCreditRepository
import com.hit11.zeus.repository.UserRepository
import com.hit11.zeus.repository.WalletTransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit


@Service
class UserService(
    private val userRepository: UserRepository,
    private val walletTransactionRepository: WalletTransactionRepository,
    private val promotionalCreditRepository: PromotionalCreditRepository,
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
        var hit11User = userRepository.findByFirebaseUID(firebaseUID)
        if (hit11User == null) {
            hit11User = createUser(firebaseUID, "")
        }
        return hit11User
    }

    fun getOrCreateUser(mobileNumber: String): User {
        val user = userRepository.findByPhone(mobileNumber)
        if (user != null) {
            return user
        }
        val createdUser = User(
            phone = mobileNumber
        )
        val savedUser = userRepository.save(createdUser)
        return savedUser
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
                firebaseUID = firebaseUser.uid,
                fcmToken = fcmToken,
                email = firebaseUser.email,
                name = firebaseUser.displayName,
                phone = firebaseUser.phoneNumber,
                depositedBalance = BigDecimal.ZERO,
                promotionalBalance = BigDecimal.ZERO,
                winningsBalance = BigDecimal.ZERO,
                reservedBalance = BigDecimal.ZERO,
                lastLoginDate = Date()
            )
            try {
                val savedUser = userRepository.save(newUser)
                // Add signup bonus
                addPromotionalCredit(
                    userId = savedUser.id,
                    amount = BigDecimal(SIGNUP_BONUS),
                    type = PromotionalType.SIGNUP_BONUS,
                    expiryDays = 7 // 7 days
                )

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
            if (diffInDays > 0L) {
                val rewardAmount = DAILY_REWARD_AMOUNT
                try {
                    addPromotionalCredit(
                        userId = userRecord.id,
                        amount = rewardAmount.toBigDecimal(),
                        type = PromotionalType.DAILY_REWARD,
                        expiryDays = 7
                    )
                    // Update last login date
                    userRecord.lastLoginDate = newLoginDate
                    userRepository.save(userRecord)

                    logger.info("Added $rewardAmount to user with ID: ${userRecord.id}")
                    return UserReward(
                        "DAILY_LOGIN_REWARD",
                        "NON_WITHDRAWAL_BALANCE",
                        rewardAmount,
                        Instant.now().epochSecond
                    )
                } catch (e: Exception) {
                    logger.error("Error adding daily reward for user ${userRecord.id}", e)
                }
            }
        }
        return null
    }

    // Order Created -> reserveBalance() -> Holds the amount
    @Transactional
    fun reserveBalance(userId: Int, amount: BigDecimal): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        if (user.availableForTrading < amount) {
            return false
        }

        user.reservedBalance += amount
        userRepository.save(user)

        // Record transaction
        walletTransactionRepository.save(
            WalletTransaction(
                user = user,
                amount = amount,
                type = TransactionType.TRADE_RESERVE,
                balanceType = BalanceType.RESERVED,
                description = "Amount reserved for trade"
            )
        )

        return true
    }

    // Order Matched -> confirmReservedBalance() -> Actually deducts the amount and releases hold
    @Transactional
    fun confirmReservedBalance(userId: Int, amount: BigDecimal) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // When orders match:
        // 1. Reduce from their actual trading balance (depositedBalance + winningsBalance + promotionalBalance)
        // Using proportional deduction based on available balances
        val totalAvailable = user.depositedBalance + user.winningsBalance + user.promotionalBalance

        // Deduct proportionally from each balance type
        if (totalAvailable > BigDecimal.ZERO) {
            val fromDeposited = (user.depositedBalance * amount) / totalAvailable
            val fromWinnings = (user.winningsBalance * amount) / totalAvailable
            val fromPromotional = (user.promotionalBalance * amount) / totalAvailable

            user.depositedBalance -= fromDeposited
            user.winningsBalance -= fromWinnings
            user.promotionalBalance -= fromPromotional
        }

        // 2. Release the reserved amount since it's now matched
        user.reservedBalance -= amount

        userRepository.save(user)

        // Record the transaction
        walletTransactionRepository.save(
            WalletTransaction(
                user = user,
                amount = amount,
                type = TransactionType.TRADE_RESERVE,
                balanceType = BalanceType.RESERVED,
                description = "Order matched and amount deducted"
            )
        )
    }

    // Order Cancelled -> releaseReservedBalance() -> Just releases hold
    @Transactional
    fun releaseReservedBalance(userId: Int, amount: BigDecimal) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        user.reservedBalance -= amount
        userRepository.save(user)
    }

    @Transactional
    fun addPromotionalCredit(
        userId: Int,
        amount: BigDecimal,
        type: PromotionalType,
        expiryDays: Int = 7
    ) {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("User not found with ID: $userId") }

        // Update user's promotional balance
        user.promotionalBalance = user.promotionalBalance.plus(amount)
        userRepository.save(user)

        promotionalCreditRepository.save(
            PromotionalCredit(
                user = user,
                amount = amount,
                type = type,
                expiryDate = Instant.now().plus(expiryDays.toLong(), ChronoUnit.DAYS),
                status = PromotionalStatus.ACTIVE
            )
        )

        walletTransactionRepository.save(
            WalletTransaction(
                user = user,
                amount = amount,
                type = TransactionType.PROMOTIONAL_CREDIT,
                balanceType = BalanceType.PROMOTIONAL,
                description = "Promotional credit added"
            )
        )
        logger.info("Added promotional credit of $amount to user ${user.id}")

    }

    @Transactional
    fun updateUserWallet(userId: Int, amount: BigDecimal, isWinning: Boolean = true): Boolean {
        val user = userRepository.findById(userId).orElseThrow {
            throw UserNotFoundException("User not found with ID: $userId")
        }

        if (isWinning) {
            // Add to winnings balance
            user.winningsBalance = user.winningsBalance.plus(amount.setScale(2))

            walletTransactionRepository.save(
                WalletTransaction(
                    user = user,
                    amount = amount,
                    type = TransactionType.TRADE_WIN,
                    balanceType = BalanceType.WINNINGS,
                    description = "Trade win credited"
                )
            )
        } else {
            // Handle loss - money was already deducted when order matched
            walletTransactionRepository.save(
                WalletTransaction(
                    user = user,
                    amount = amount,
                    type = TransactionType.TRADE_LOSS,
                    balanceType = BalanceType.DEPOSITED,
                    description = "Trade loss recorded"
                )
            )
        }

        userRepository.save(user)
        return true
    }



    @Transactional
    fun addDeposit(userId: Int, amount: BigDecimal, referenceId: String): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        user.depositedBalance += amount
        userRepository.save(user)

        walletTransactionRepository.save(
            WalletTransaction(
                user = user,
                amount = amount,
                type = TransactionType.DEPOSIT,
                balanceType = BalanceType.DEPOSITED,
                description = "Money added to wallet",
                referenceId = referenceId
            )
        )
        return true
    }

    @Transactional
    fun handleWithdrawal(userId: Int, amount: BigDecimal): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        if (user.winningsBalance < amount) {
            return false
        }

        user.winningsBalance -= amount
        userRepository.save(user)

        walletTransactionRepository.save(
            WalletTransaction(
                user = user,
                amount = amount,
                type = TransactionType.WITHDRAWAL,
                balanceType = BalanceType.WINNINGS,
                description = "Withdrawal to bank account"
            )
        )
        return true
    }

    // Function to handle promotional credit expiry
    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    fun handlePromotionalCreditExpiry() {
        val expiredCredits = promotionalCreditRepository
            .findByExpiryDateBeforeAndStatus(
                Instant.now(),
                PromotionalStatus.ACTIVE
            )

        expiredCredits.forEach { credit ->
            val user = credit.user
            user.promotionalBalance -= credit.amount
            credit.status = PromotionalStatus.EXPIRED

            // Save changes
            userRepository.save(user)
            promotionalCreditRepository.save(credit)

            // Record expiry transaction
            walletTransactionRepository.save(
                WalletTransaction(
                    user = user,
                    amount = credit.amount,
                    type = TransactionType.PROMOTIONAL_EXPIRY,
                    balanceType = BalanceType.PROMOTIONAL,
                    description = "Promotional credit expired",
                    referenceId = credit.id.toString()
                )
            )
        }
    }

    fun getTransactionHistory(userId: Int): List<WalletTransaction> {
        return walletTransactionRepository.findByUserId(userId)
    }

    fun getActivePromotionalCredits(userId: Int): List<PromotionalCredit> {
        return promotionalCreditRepository.findByUserIdAndStatus(
            userId,
            PromotionalStatus.ACTIVE
        )
    }

    companion object {
        private const val DAILY_REWARD_AMOUNT = 200.0
        private const val SIGNUP_BONUS = 500.0
        private const val MINIMUM_DAYS_BETWEEN_REWARDS = 1L
    }
}
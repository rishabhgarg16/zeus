package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*
import javax.persistence.*

// User.kt
@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "firebase_uid", nullable = false, unique = true)
    val firebaseUID: String = "",
    var fcmToken: String? = null,
    val email: String? = "",
    val name: String? = "",
    val phone: String = "",

    // Main wallet balances
    @Column(name = "deposited_balance", precision = 19, scale = 4)
    var depositedBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "winnings_balance", precision = 19, scale = 4)
    var winningsBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "promotional_balance", precision = 19, scale = 4)
    var promotionalBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "reserved_balance", precision = 19, scale = 4)
    var reservedBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "last_login_date")
    var lastLoginDate: Date? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    // Computed properties
    val totalBalance: BigDecimal
        get() = depositedBalance + winningsBalance + promotionalBalance

    val availableForTrading: BigDecimal
        get() = (depositedBalance + winningsBalance + promotionalBalance) - reservedBalance

    val availableForWithdrawal: BigDecimal
        get() = winningsBalance

    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}

// PromotionalCredit.kt
@Entity
@Table(name = "promotional_credits")
data class PromotionalCredit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(precision = 19, scale = 4)
    val amount: BigDecimal,

    val expiryDate: Instant,

    @Enumerated(EnumType.STRING)
    val type: PromotionalType,

    @Enumerated(EnumType.STRING)
    var status: PromotionalStatus = PromotionalStatus.ACTIVE,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)

enum class PromotionalType {
    SIGNUP_BONUS,
    DAILY_REWARD,
    REFERRAL_BONUS,
    SPECIAL_OFFER
}

enum class PromotionalStatus {
    ACTIVE,
    USED,
    EXPIRED
}

// WalletTransaction.kt
@Entity
@Table(name = "wallet_transactions")
data class WalletTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    @Column(precision = 19, scale = 4)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    @Enumerated(EnumType.STRING)
    val balanceType: BalanceType,

    val description: String,

    @Column(name = "reference_id")
    val referenceId: String? = null,  // For payment gateway reference etc.

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)


@Entity
@Table(name = "wallet_transactions")
data class WalletTransactionRow(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id")
    val userId: Int? = null,

    @Column(precision = 19, scale = 4)
    val amount: BigDecimal = BigDecimal(0),

    @Enumerated(EnumType.STRING)
    val type: TransactionType? = null,

    @Enumerated(EnumType.STRING)
    val balanceType: BalanceType? = null,

    val description: String = "",

    @Column(name = "reference_id")
    val referenceId: String? = null,  // For payment gateway reference etc.

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRADE_RESERVE,
    TRADE_RELEASE,
    TRADE_WIN,
    TRADE_LOSS,
    PROMOTIONAL_CREDIT,
    PROMOTIONAL_EXPIRY
}

enum class BalanceType {
    DEPOSITED,
    WINNINGS,
    PROMOTIONAL,
    RESERVED
}


enum class TransactionStatus {
    INITIATED,
    ACCEPTED,
    DECLINED
}

@Entity
@Table(name = "payment_transactions")
data class PaymentTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Int,

    @Column(precision = 19, scale = 4, nullable = false)
    val amount: BigDecimal,

    @Column(name = "transaction_id", nullable = false, unique = true)
    val transactionId: String,

    @Column(nullable = true)
    val metadata: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TransactionStatus = TransactionStatus.INITIATED,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)

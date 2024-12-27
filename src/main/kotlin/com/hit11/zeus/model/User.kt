package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "firebase_uid", nullable = false, unique = true)
    val firebaseUID: String = "",
    val fcmToken: String? = null,
    val email: String? = "",
    val name: String? = "",
    val phone: String = "",

    @Column(name = "wallet_balance", precision = 19, scale = 4)
    var walletBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "reserved_balance", precision = 19, scale = 4)
    var reservedBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "withdrawal_balance", precision = 19, scale = 4)
    var withdrawalBalance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "last_login_date", nullable = true, updatable = true)
    var lastLoginDate: Date? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

) {
    val availableBalance: BigDecimal
        get() = walletBalance - reservedBalance


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
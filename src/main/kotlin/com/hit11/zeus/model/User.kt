package com.hit11.zeus.model

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Table

data class User(
    val id: Int  = 0,
    val firebaseUID: String = "",
    val email: String? = "",
    val name: String? = "",
    val phone: String = "",
    val walletBalance: Double = 0.0,
    val withdrawalBalance: Double = 0.0,
)

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "firebase_uid", nullable = false, unique = true)
    val firebaseUID: String = "",
    val email: String? = "",
    val name: String? = "",
    val phone: String = "",
    var walletBalance: Double = 0.0,
    var withdrawalBalance: Double = 0.0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),


    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

) {
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
fun mapToUser(userEntity: UserEntity): User {
    return User(
        id = userEntity.id,
        firebaseUID = userEntity.firebaseUID,
        email = userEntity.email,
        name = userEntity.name,
        phone = userEntity.phone,
        walletBalance = userEntity.walletBalance,
        withdrawalBalance = userEntity.withdrawalBalance,
    )
}
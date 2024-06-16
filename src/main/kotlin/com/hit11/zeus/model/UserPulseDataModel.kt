package com.hit11.zeus.model

import java.time.Instant
import javax.persistence.*

data class UserPulseDataModel(
    val userId: Int = 0,
    val pulseId: Int = 0,
    val matchId: Int = 0,
    val userAnswer: String = "",
    val answerTime: Instant = Instant.now(),
    val userWager: Double = -1.0,
    val quantity: Long = 0L,
    val tradeAmount: Double = 0.0,
    var userResult: String? = UserResult.ACTIVE.text,

    ) {
    fun checkIfUserWon(userAnswer: String, pulseDataModel: PulseDataModel): String {
        return when {
            pulseDataModel.enabled -> UserResult.ACTIVE.text
            pulseDataModel.pulseResult.isNullOrBlank() -> UserResult.ACTIVE.text
            userAnswer == pulseDataModel.pulseResult -> UserResult.WIN.text
            else -> UserResult.LOSE.text
        }
    }

    fun toEntity(): OrderEntity {
        return OrderEntity(
            userId = this.userId,
            pulseId = this.pulseId,
            matchId = this.matchId,
            userAnswer = this.userAnswer,
            answerTime = Instant.now(),
            userWager = this.userWager,
            userResult = this.userResult,
            quantity = this.quantity,
            tradeAmount = this.tradeAmount
        )
    }
}

@Entity
@Table(name = "orders")
data class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val userId: Int = 0,
    val pulseId: Int = 0,
    val matchId: Int = 0,
    var userAnswer: String = "",
    val answerTime: Instant = Instant.now(),
    val userWager: Double = 0.0,
    var userResult: String? = UserResult.ACTIVE.text,
    var quantity: Long = 0L,
    var tradeAmount: Double = 0.0,

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

fun OrderEntity.toDataModel(): UserPulseDataModel {
    return UserPulseDataModel(
        userId = this.userId,
        pulseId = this.pulseId,
        matchId = this.matchId,
        userAnswer = this.userAnswer,
        answerTime = this.answerTime,
        userWager = this.userWager,
        userResult = this.userResult
    )
}


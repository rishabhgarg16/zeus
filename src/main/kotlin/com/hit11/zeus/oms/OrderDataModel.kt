package com.hit11.zeus.oms

import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

enum class OrderState {
    OPEN,
    PARTIALLY_MATCHED,
    FULLY_MATCHED,
    CANCELLED,
    EXPIRED
}

data class OrderDataModel(
    val id : Int = 0,
    val userId: Int = 0,
    val pulseId: Int = 0,
    val matchId: Int = 0,
    val userAnswer: String = "",
    val answerTime: Instant = Instant.now(),
    @Column(name = "price", precision = 10, scale = 2)
    val price: BigDecimal = BigDecimal.valueOf(0),
    var quantity: Long = 0L,
    val totalAmount: BigDecimal = BigDecimal.valueOf(0),
    var state: OrderState = OrderState.OPEN,
    var remainingQuantity: Long = 0L
) {


    fun toEntity(): OrderEntity {
        return OrderEntity(
            userId = this.userId,
            pulseId = this.pulseId,
            matchId = this.matchId,
            userAnswer = this.userAnswer,
            answerTime = Instant.now(),
            price = this.price,
            state = this.state,
            quantity = this.quantity,
            remainingQuantity = this.remainingQuantity,
            totalAmount = this.totalAmount
        )
    }

    fun updateState() {
        state = when {
            remainingQuantity == quantity -> OrderState.FULLY_MATCHED
            remainingQuantity > 0 -> OrderState.PARTIALLY_MATCHED
            else -> state
        }
    }
}

@Entity
@Table(name = "orders")
data class OrderEntity(
    val userId: Int = 0,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val pulseId: Int = 0,
    val matchId: Int = 0,
    var userAnswer: String = "",
    val answerTime: Instant = Instant.now(),
    val price: BigDecimal = BigDecimal.valueOf(0),
    var quantity: Long = 0L,
    var totalAmount: BigDecimal = BigDecimal.valueOf(0),
    @Enumerated(EnumType.STRING)
    var state: OrderState = OrderState.OPEN,
    var remainingQuantity: Long = 0L,

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

fun OrderEntity.toDataModel(): OrderDataModel {
    return OrderDataModel(
        id = this.id,
        userId = this.userId,
        pulseId = this.pulseId,
        matchId = this.matchId,
        userAnswer = this.userAnswer,
        answerTime = this.answerTime,
        price = this.price,
        quantity = this.quantity,
        remainingQuantity = this.remainingQuantity,
        state = this.state,
        totalAmount = this.totalAmount,
    )
}


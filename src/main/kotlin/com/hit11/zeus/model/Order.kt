package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

enum class OrderStatus { OPEN, PARTIALLY_FILLED, FILLED, CANCELLED, EXPIRED }
enum class OrderType { BUY, SELL, UNKNOWN }
enum class OrderExecutionType { MARKET, LIMIT, UNKNOWN }
enum class OrderSide {
    UNKNOWN, Yes, No
}

@Entity
@Table(name = "orders")
data class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Int = -1,

    // Direct ID fields for simple queries
    @Column(name = "match_id")
    val matchId: Int = 0,

    @Column(name = "pulse_id")
    val pulseId: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "match_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false
    )
    val match: Match = Match(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "pulse_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false
    )
    val pulse: Question = Question(),

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type")
    val orderType: OrderType = OrderType.BUY,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side")
    val orderSide: OrderSide = OrderSide.UNKNOWN,

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type")
    val executionType: OrderExecutionType = OrderExecutionType.UNKNOWN,

    @Column(precision = 10, scale = 2)
    val price: BigDecimal = 0.toBigDecimal(),

    val quantity: Long = 0,

    var remainingQuantity: Long = 0,

    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.OPEN,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    val isBuyOrder: Boolean
        get() = orderType == OrderType.BUY

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
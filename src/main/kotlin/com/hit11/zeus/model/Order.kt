package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

enum class OrderStatus { OPEN, PARTIALLY_FILLED, FILLED, CANCELLED, EXPIRED }
enum class OrderType { BUY, SELL }
enum class OrderExecutionType { MARKET, LIMIT, UNKNOWN }
enum class OrderSide {
    UNKNOWN ,Yes, No
}

@Entity
@Table(name = "orders")
data class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Int = -1,

    val pulseId: Int = -1,

    val matchId: Int = -1,

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

//    fun toDataModel() = Order(
//        id = id,
//        userId = userId,
//        pulseId = pulseId,
//        matchId = matchId,
//        orderType = orderType,
//        orderSide = orderSide,
//        price = price,
//        quantity = quantity,
//        remainingQuantity = remainingQuantity,
//        state = state,
//        totalAmount = totalAmount
//    )
}
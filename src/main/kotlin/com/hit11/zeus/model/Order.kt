package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

enum class OrderStatus { OPEN, PARTIALLY_FILLED, FILLED, CANCELLED, EXPIRED }
enum class OrderType { BUY, SELL }
enum class OrderExecutionType { MARKET, LIMIT }
enum class OrderSide(val text: String) {
    YES("Yes"),
    NO("No") ;

    companion object {
        fun fromString(name: String): OrderSide {

            val orderSide = entries.find {
                it.text == name
            } ?: throw IllegalArgumentException("Invalid OrderSide: $name")

            return orderSide
        }
    }
}

@Entity
@Table(name = "orders")
data class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: Int,

    val pulseId: Int,

    val matchId: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type")
    val orderType: OrderType,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side")
    val orderSide: OrderSide,

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type")
    val executionType: OrderExecutionType,

    @Column(precision = 10, scale = 2)
    val price: BigDecimal,

    val quantity: Long,

    var remainingQuantity: Long,

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
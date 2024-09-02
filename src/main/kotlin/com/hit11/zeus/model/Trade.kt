package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

enum class TradeType { BUY, SELL }

@Entity
@Table(
    name = "trades", indexes = [
        Index(name = "idx_pulse_id", columnList = "pulse_id"),
        Index(name = "idx_match_id", columnList = "match_id"),
        Index(name = "idx_buy_order_id", columnList = "buy_order_id"),
        Index(name = "idx_sell_order_id", columnList = "sell_order_id"),
    ]
)
data class Trade(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "buy_order_id")
    val buyOrderId: Long,

    @Column(name = "sell_order_id")
    val sellOrderId: Long,

    @Column(name = "pulse_id")
    val pulseId: Int,

    @Column(name = "match_id")
    val matchId: Int,

    @Column(name = "quantity")
    val quantity: Long,

    @Column(name = "price")
    val price: BigDecimal,

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


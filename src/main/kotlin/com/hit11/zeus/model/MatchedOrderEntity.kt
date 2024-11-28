package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

enum class TradeType { BUY, SELL }

@Entity
@Table(
    name = "order_executions", indexes = [
        Index(name = "idx_pulse_id", columnList = "pulse_id"),
        Index(name = "idx_match_id", columnList = "match_id"),
        Index(name = "idx_yes_order_id", columnList = "yes_order_id"),
        Index(name = "idx_no_order_id", columnList = "no_order_id"),
    ]
)
data class MatchedOrderEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "yes_order_id")
    val yesOrderId: Long = 0,

    @Column(name = "no_order_id")
    val noOrderId: Long = 0,

    @Column(name = "pulse_id")
    val pulseId: Int = 0,

    @Column(name = "match_id")
    val matchId: Int = 0,

    @Column(name = "matched_quantity")
    val matchedQuantity: Long = 0,

    @Column(name = "matched_yes_price")
    val matchedYesPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "matched_no_price")
    val matchedNoPrice: BigDecimal = BigDecimal.ZERO,

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


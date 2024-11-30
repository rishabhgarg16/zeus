package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

enum class PositionStatus { OPEN, CLOSED }

@Entity
@Table(
    name = "positions",
    indexes = [
        Index(name = "idx_user_id_pulse_id_side", columnList = "user_id, pulse_id, order_side")
    ]
)
data class UserPosition(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id")
    val userId: Int = 0,

    @Column(name = "pulse_id")
    val pulseId: Int = 0,

    @Column(name = "match_id")
    val matchId: Int = 0,

    @Column(name = "quantity")
    var quantity: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side")
    val orderSide: OrderSide = OrderSide.UNKNOWN, // Yes or No

    @Column(name = "average_price")
    var averagePrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "realized_pnl")
    var realizedPnl: BigDecimal = BigDecimal.ZERO,

    @Column(name = "unrealized_pnl")
    var unrealizedPnl: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: PositionStatus = PositionStatus.OPEN,

    @Column(name = "close_time")
    var closeTime: Instant? = null,

    @Column(name = "settled_amount")
    var settledAmount: BigDecimal? = null, // Store the final computed payout

    @Column(name = "created_at", updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
)

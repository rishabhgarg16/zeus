package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

enum class PositionStatus { OPEN, CLOSED }
@Entity
@Table(
    name = "positions",
    indexes = [
        Index(name = "idx_user_id_pulse_id", columnList = "user_id, pulse_id")
    ]
)
data class UserPosition(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id")
    val userId: Int = 0,

    @Column(name = "pulse_id")
    val pulseId: Int=0,

    @Column(name = "yes_quantity")
    var yesQuantity: Long = 0,

    @Column(name = "no_quantity")
    var noQuantity: Long = 0,

    @Column(name = "average_yes_price")
    var averageYesPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "average_no_price")
    var averageNoPrice: BigDecimal = BigDecimal.ZERO,

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
    var settledAmount: BigDecimal? = null // Store the final computed payout
)

package com.hit11.zeus.model

import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

@Entity
@Table(
    name = "trades",
    indexes = [
        Index(name = "idx_user_id_pulse_id", columnList = "user_id, pulse_id"),
        Index(name = "idx_order_id", columnList = "order_id"),
        Index(name = "idx_match_id", columnList = "match_id"),
    ]
)
data class Trade(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "user_id")
    val userId: Int = 0,
    @Column(name = "order_id")
    val orderId: Long = 0,
    @Column(name = "match_id")
    val matchId: Int = 0,
    @Column(name = "pulse_id")
    val pulseId: Int = 0,
    @Enumerated(EnumType.STRING)
    val side: OrderSide = OrderSide.UNKNOWN,
    val quantity: Long = 0,
    val price: BigDecimal = BigDecimal.ZERO,
    val amount: BigDecimal = BigDecimal.ZERO,
    @Enumerated(EnumType.STRING)
    var status: TradeStatus = TradeStatus.ACTIVE,
    @Column(name = "pnl", precision = 10, scale = 2)
    var pnl: BigDecimal? = null,
    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "settled_at")
    var settledAt: Instant? = null
) {
    fun checkIfUserWon(userAnswer: OrderSide, pulseResult: PulseResult): String {
        return when {
            pulseResult == PulseResult.UNDECIDED -> TradeResult.ACTIVE.text
            (userAnswer == OrderSide.Yes && pulseResult == PulseResult.Yes) ||
                    (userAnswer == OrderSide.No && pulseResult == PulseResult.No) -> TradeResult.WIN.text

            else -> TradeResult.LOSE.text
        }
    }
}

enum class TradeResult(val text: String, val outcome: Int) {
    WIN("Win", 1),
    LOSE("Lose", 2),
    ACTIVE("Active", 3);

    companion object {
        fun fromText(userResult: String?): TradeResult {
            when (userResult) {
                "Win" -> return WIN
                "Lose" -> return LOSE
                "Active" -> return ACTIVE
            }
            return ACTIVE
        }
    }
}

enum class TradeStatus {
    ACTIVE, WON, LOST, CANCELLED
}
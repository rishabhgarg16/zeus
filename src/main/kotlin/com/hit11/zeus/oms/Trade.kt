package com.hit11.zeus.oms

import com.hit11.zeus.model.QuestionDataModel
import java.math.BigDecimal
import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "trades", indexes = [
    Index(name = "idx_user_id", columnList = "user_id"),
    Index(name = "idx_pulse_id", columnList = "pulse_id"),
    Index(name = "idx_match_id", columnList = "match_id")
])
data class Trade(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id")
    val userId: Int = 0,

    @Column(name = "pulse_id")
    val pulseId: Int = 0,

    @Column(name = "match_id")
    val matchId: Int = 0,

    @Column(name = "order_id")
    val orderId: Int = 0,

    @Column(name = "trade_quantity")
    val tradeQuantity: Long = 0,

    @Column(name = "trade_price")
    val tradePrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "trade_amount")
    val tradeAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "trade_time")
    val tradeTime: Instant = Instant.now(),

    @Column(name = "user_answer")
    val userAnswer: String = "",

    @Column(name = "result")
    @Enumerated(EnumType.STRING)
    var result: TradeResult = TradeResult.ACTIVE,

    @Version
    @Column(name = "version")
    var version: Long = 0,

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

    fun checkIfUserWon(userAnswer: String, questionDataModel: QuestionDataModel): String {
        return when {
            questionDataModel.enabled -> TradeResult.ACTIVE.text
            questionDataModel.pulseResult.isNullOrBlank() -> TradeResult.ACTIVE.text
            userAnswer == questionDataModel.pulseResult -> TradeResult.WIN.text
            else -> TradeResult.LOSE.text
        }
    }
}
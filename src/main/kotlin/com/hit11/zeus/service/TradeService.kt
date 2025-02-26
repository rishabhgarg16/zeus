package com.hit11.zeus.service

import com.hit11.zeus.model.*
import com.hit11.zeus.notification.DeliveryType
import com.hit11.zeus.notification.NotificationPayload
import com.hit11.zeus.notification.NotificationService
import com.hit11.zeus.notification.NotificationType
import com.hit11.zeus.repository.MatchRepository
import com.hit11.zeus.repository.QuestionRepository
import com.hit11.zeus.repository.TradeRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Service
class TradeService(
    private val tradeRepository: TradeRepository,
    private val matchRepository: MatchRepository,
    private val questionRepository: QuestionRepository,
    private val notificationService: NotificationService,
) {
    private val logger = LoggerFactory.getLogger(TradeService::class.java)

    @Transactional
    fun createTrade(orderMatch: OrderMatch) {
        val trades = listOf(
            Trade(
                userId = orderMatch.yesOrder.userId,
                orderId = orderMatch.yesOrder.id,
                pulseId = orderMatch.yesOrder.pulseId,
                matchId = orderMatch.yesOrder.matchId,
                side = OrderSide.Yes,
                price = orderMatch.matchedYesPrice,
                quantity = orderMatch.matchedQuantity,
                amount = orderMatch.matchedYesPrice.multiply(BigDecimal(orderMatch.matchedQuantity)),
                status = TradeStatus.ACTIVE
            ),
            Trade(
                userId = orderMatch.noOrder.userId,
                orderId = orderMatch.noOrder.id,
                pulseId = orderMatch.noOrder.pulseId,
                matchId = orderMatch.noOrder.matchId,
                side = OrderSide.No,
                price = BigDecimal.TEN.subtract(orderMatch.matchedYesPrice),
                quantity = orderMatch.matchedQuantity,
                amount = orderMatch.matchedNoPrice.multiply(BigDecimal(orderMatch.matchedQuantity)),
                status = TradeStatus.ACTIVE
            )
        )
        // Save the trades in the database
        val savedTrades = tradeRepository.saveAll(trades)

        // Send notifications asynchronously for each trade
        savedTrades.forEach { trade ->
            notificationService.notifyTradeCreated(trade)
        }
    }

    @Transactional
    fun settleTradesByPulse(pulseId: Int, pulseResult: PulseResult) {
        val trades = tradeRepository.findByPulseIdAndStatus(pulseId, TradeStatus.ACTIVE)
        if (trades.isEmpty()) {
            logger.info("No active trades to close for Pulse $pulseId")
            return
        }

        val updatedTrades = trades.map { trade ->
            trade.apply {
                status = if (checkIfUserWon(side, pulseResult) == TradeResult.WIN.text) {
                    TradeStatus.WON
                } else {
                    TradeStatus.LOST
                }
                pnl = calculatePnl(trade, pulseResult)
                settledAt = Instant.now()
            }
        }

        // Save updated trades in a batch
        tradeRepository.saveAll(updatedTrades)
        sendTradeNotification(updatedTrades)
        logger.info("Successfully closed ${updatedTrades.size} trades for Pulse $pulseId")
    }

    private fun sendTradeNotification(trades: List<Trade>) {
        trades.map { trade ->
            val deliveryType = DeliveryType.BOTH // You can customize this as needed
            val title = if (trade.status == TradeStatus.WON) "Congratulations! You Won 🎉" else "Better Luck Next Time!"
            val message = if (trade.status == TradeStatus.WON) {
                "Your trade on Pulse ${trade.pulseId} was a success. You've won ₹${trade.pnl}!"
            } else {
                "Your trade on Pulse ${trade.pulseId} did not succeed. Trade more to win next time!"
            }

            val payload = NotificationPayload(
                userId = trade.userId,
                type = NotificationType.TRADE_SETTLED,
                title = title,
                message = message,
                metadata = mapOf(
                    "tradeId" to trade.id.toString(),
                    "pulseId" to trade.pulseId.toString(),
                    "status" to trade.status.name
                ),
                deliveryType = deliveryType
            )

            notificationService.sendNotificationAsync(payload)
        }

    }

    private fun calculatePnl(trade: Trade, pulseResult: PulseResult): BigDecimal {
        return when {
            pulseResult == PulseResult.Yes && trade.side == OrderSide.Yes -> {
                (BigDecimal.TEN.subtract(trade.price)).multiply(BigDecimal(trade.quantity))
            }

            pulseResult == PulseResult.No && trade.side == OrderSide.No -> {
                (BigDecimal.TEN.subtract(trade.price)).multiply(BigDecimal(trade.quantity))
            }

            else -> BigDecimal.ZERO
        }
    }

    // Get all trades for a given pulse ID
    fun getTradesByPulse(pulseId: Int): List<Trade> {
        return tradeRepository.findByPulseId(pulseId)
    }

    // Get all trades for a given match ID
    fun getTradesByMatch(matchId: Int): List<Trade> {
        return tradeRepository.findByMatchId(matchId)
    }

    // Get all trades for a given order (yesOrderId or noOrderId)
    fun getTradesByOrder(orderId: Long): List<Trade> {
        return tradeRepository.findByOrderId(orderId)
    }

    fun getTradesByUser(userId: Long): List<Trade> {
        return tradeRepository.findByOrderId(userId)
    }

    // Get the most recent trades for a pulse ID (e.g., last 10 trades)
    fun getRecentTradesByPulse(pulseId: Int, limit: Int): List<Trade> {
        return tradeRepository.findTopByPulseIdOrderByCreatedAtDesc(pulseId, limit) ?: emptyList()
    }

    // Get trades within a date range for a pulse ID
    fun getTradesByPulseAndDateRange(pulseId: Int, startDate: Instant, endDate: Instant): List<Trade> {
        return tradeRepository.findByPulseIdAndCreatedAtBetween(pulseId, startDate, endDate)
    }

    /**
     * Fetch all live trades (active) for a user.
     */
    fun getLiveTrades(userId: Int, pageable: Pageable): List<UiMyTradesResponse>? {
        val trades = tradeRepository.findByUserIdAndStatus(userId, TradeStatus.ACTIVE, pageable)
        val pulseIds = trades.map { it.pulseId }.distinct()
        val matchIds = trades.map { it.matchId }.distinct()

        // Fetch questions & matches in batch
        val questions = questionRepository.findAllById(pulseIds).associateBy { it.id }
        val matches = matchRepository.findAllById(matchIds).associateBy { it.id }

        // Map trades efficiently
        return trades.mapNotNull { trade ->
            val question = questions[trade.pulseId] ?: return@mapNotNull null
            val match = matches[trade.matchId] ?: return@mapNotNull null
            trade.toUiMyTradesResponse(question, match)
        }
    }

    /**
     * Fetch paginated completed trades (for infinite scrolling).
     */
    fun getCompletedTrades(userId: Int, pageable: Pageable): List<UiMyTradesResponse>? {
        val trades = tradeRepository.findByUserIdAndStatusIn(
            userId,
            listOf(TradeStatus.WON, TradeStatus.LOST),
            pageable
        )

        // Get all pulseIds & matchIds in one call
        val pulseIds = trades.map { it.pulseId }.distinct()
        val matchIds = trades.map { it.matchId }.distinct()

        // Fetch questions & matches in batch
        val questions = questionRepository.findAllById(pulseIds).associateBy { it.id }
        val matches = matchRepository.findAllById(matchIds).associateBy { it.id }

        // Map trades efficiently
        return trades.mapNotNull { trade ->
            val question = questions[trade.pulseId] ?: return@mapNotNull null
            val match = matches[trade.matchId] ?: return@mapNotNull null
            trade.toUiMyTradesResponse(question, match)
        }
    }
}
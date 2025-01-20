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
    fun createTrade(orderMatch: OrderMatch, avgEntryPrice: BigDecimal?) {
        val trades = listOf(
            Trade(
                userId = orderMatch.yesOrder.userId,
                orderId = orderMatch.yesOrder.id,
                pulseId = orderMatch.yesOrder.pulseId,
                matchId = orderMatch.yesOrder.matchId,
                orderSide = OrderSide.Yes,
                isBuyOrder = orderMatch.yesOrder.isBuyOrder,  // Track buy/sell
                averageEntryPrice = if (!orderMatch.yesOrder.isBuyOrder) avgEntryPrice else null,
                price = orderMatch.matchedYesPrice,
                quantity = if (orderMatch.yesOrder.isBuyOrder)
                    orderMatch.matchedQuantity
                else
                    -orderMatch.matchedQuantity,  // Negative for sells
                amount = orderMatch.matchedYesPrice.multiply(BigDecimal(orderMatch.matchedQuantity)),
                // For exits, calculate immediate realized PNL
                realizedPnl = if (!orderMatch.yesOrder.isBuyOrder && avgEntryPrice != null) {
                    orderMatch.matchedYesPrice.subtract(avgEntryPrice)
                        .multiply(BigDecimal(orderMatch.matchedQuantity))
                } else null,
                status = if (!orderMatch.yesOrder.isBuyOrder)
                    TradeStatus.CLOSED  // Exit trades are closed immediately
                else
                    TradeStatus.ACTIVE
            ),
            Trade(
                userId = orderMatch.noOrder.userId,
                orderId = orderMatch.noOrder.id,
                pulseId = orderMatch.noOrder.pulseId,
                matchId = orderMatch.noOrder.matchId,
                orderSide = OrderSide.No,
                isBuyOrder = orderMatch.noOrder.isBuyOrder, // Track buy/sell
                price = orderMatch.matchedNoPrice,
                quantity = if (orderMatch.noOrder.isBuyOrder)
                    orderMatch.matchedQuantity
                else
                    -orderMatch.matchedQuantity,  // Negative for sells
                amount = orderMatch.matchedNoPrice.multiply(BigDecimal(orderMatch.matchedQuantity)),
                averageEntryPrice = if (!orderMatch.noOrder.isBuyOrder) avgEntryPrice else null,
                realizedPnl = if (!orderMatch.noOrder.isBuyOrder && avgEntryPrice != null) {
                    orderMatch.matchedNoPrice.subtract(avgEntryPrice)
                        .multiply(BigDecimal(orderMatch.matchedQuantity))
                } else null,
                status = if (!orderMatch.noOrder.isBuyOrder)
                    TradeStatus.CLOSED
                else
                    TradeStatus.ACTIVE
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
        // Only get active buy trades - exit trades are already settled
        val trades = tradeRepository.findByPulseIdAndStatusAndIsBuyOrder(
            pulseId,
            TradeStatus.ACTIVE,
            isBuyOrder = true
        )

        if (trades.isEmpty()) {
            logger.info("No active trades to close for Pulse $pulseId")
            return
        }

        val updatedTrades = trades.map { trade ->
            trade.apply {
                status = if (checkIfUserWon(pulseResult) == TradeResult.WIN.text) {
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
        logger.info("Successfully closed ${updatedTrades.size} buy trades for Pulse $pulseId")
    }

    private fun sendTradeNotification(trades: List<Trade>) {
        trades.map { trade ->
            val deliveryType = DeliveryType.BOTH

            val (title, message) = when {
                !trade.isBuyOrder -> {
                    // For exit trades
                    val profitOrLoss = trade.realizedPnl ?: BigDecimal.ZERO
                    if (profitOrLoss > BigDecimal.ZERO) {
                        Pair(
                            "Position Closed - Profit! 🎯",
                            "Successfully exited position on Pulse ${trade.pulseId}. Profit: ₹$profitOrLoss"
                        )
                    } else {
                        Pair(
                            "Position Closed",
                            "Exited position on Pulse ${trade.pulseId}. Loss: ₹${profitOrLoss.abs()}"
                        )
                    }
                }

                trade.status == TradeStatus.WON -> Pair(
                    "Congratulations! You Won 🎉",
                    "Your trade on Pulse ${trade.pulseId} was a success. You've won ₹${trade.pnl}!"
                )

                else -> Pair(
                    "Better Luck Next Time!",
                    "Your trade on Pulse ${trade.pulseId} did not succeed. Trade more to win next time!"
                )
            }

            val payload = NotificationPayload(
                userId = trade.userId,
                type = NotificationType.TRADE_SETTLED,
                title = title,
                message = message,
                metadata = mapOf(
                    "tradeId" to trade.id.toString(),
                    "pulseId" to trade.pulseId.toString(),
                    "status" to trade.status.name,
                    "isExit" to (!trade.isBuyOrder).toString(),
                    "realizedPnl" to (trade.realizedPnl?.toString() ?: "0")
                ),
                deliveryType = deliveryType
            )

            notificationService.sendNotificationAsync(payload)
        }
    }

    private fun calculatePnl(trade: Trade, pulseResult: PulseResult): BigDecimal {
        if (trade.isBuyOrder) {
            return when {
                pulseResult == PulseResult.Yes && trade.orderSide == OrderSide.Yes -> {
                    (BigDecimal.TEN.subtract(trade.price)).multiply(BigDecimal(trade.quantity))
                }

                pulseResult == PulseResult.No && trade.orderSide == OrderSide.No -> {
                    (BigDecimal.TEN.subtract(trade.price)).multiply(BigDecimal(trade.quantity))
                }

                else -> BigDecimal.ZERO
            }
        } else {
            // For exits, PNL is immediate profit/loss on the sale
            // Note: quantity is already negative for sells
            return trade.price.subtract(trade.averageEntryPrice).multiply(BigDecimal(-trade.quantity))
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

    fun getMyTradesResponse(
        userId: Int,
        matchIdList: List<Int>,
        pageable: Pageable
    ): List<UiMyTradesResponse>? {
        try {
            val allTrades = tradeRepository.findByUserIdAndMatchIdIn(
                userId,
                matchIdList,
                pageable
            )

            val questions = questionRepository.findAllByMatchIdIn(matchIdList)
            val activeStatuses =
                listOf(MatchStatus.SCHEDULED.text, MatchStatus.IN_PROGRESS.text, MatchStatus.PREVIEW.text)
            val matches = matchRepository.findAllByIdInAndStatusIn(matchIdList, activeStatuses)

            val questionMap = questions.associateBy { it.id }
            val matchMap = matches.associateBy { it.id }

            return allTrades.mapNotNull { trade ->
                val question = questionMap[trade.pulseId] ?: return@mapNotNull null
                val match = matchMap[trade.matchId] ?: return@mapNotNull null

                trade.toUiMyTradesResponse(
                    question = question,
                    match = match
                )
            }
        } catch (e: Exception) {
            throw e
        }
    }

}
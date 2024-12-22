package com.hit11.zeus.service

import com.hit11.zeus.model.*
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
    private val questionRepository: QuestionRepository,
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
        tradeRepository.saveAll(trades)
    }

    @Transactional
    fun closeTradesByPulse(pulseId: Int, pulseResult: PulseResult) {
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
        logger.info("Successfully closed ${updatedTrades.size} trades for Pulse $pulseId")
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

            val matchQuestions = questionRepository
                .findAllByMatchIdIn(matchIdList)
                .map { it.mapToQuestionDataModel() }

            val questionIdToQuestionMap: Map<Int, QuestionDataModel> =
                matchQuestions.associateBy { it.id }.mapValues { it.value }

            return allTrades.mapNotNull { trade ->
                questionIdToQuestionMap[trade.pulseId]?.let {
                    trade.toUiMyTradesResponse(
                        it
                    )
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

}
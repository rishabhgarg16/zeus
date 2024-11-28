package com.hit11.zeus.service

import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository
import com.hit11.zeus.repository.TradeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.Instant

@Service
class TradeService(
    private val tradeRepository: TradeRepository,
    private val questionRepository: QuestionRepository,
) {
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
                amount = orderMatch.matchedYesPrice.multiply(BigDecimal(orderMatch.matchedQuantity))
            ),
            Trade(
                userId = orderMatch.noOrder.userId,
                orderId = orderMatch.noOrder.id,
                pulseId = orderMatch.noOrder.pulseId,
                matchId = orderMatch.noOrder.matchId,
                side = OrderSide.No,
                price = BigDecimal.TEN.subtract(orderMatch.matchedYesPrice),
                quantity = orderMatch.matchedQuantity,
                amount = orderMatch.matchedNoPrice.multiply(BigDecimal(orderMatch.matchedQuantity))
            )
        )
        // Save the trades in the database
        tradeRepository.saveAll(trades)
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
                    trade.toUiUserPositionsResponse(
                        it
                    )
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

}
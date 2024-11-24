package com.hit11.zeus.service

import com.hit11.zeus.model.*
import com.hit11.zeus.repository.TradeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Service
class TradeService(
    private val tradeRepository: TradeRepository,
    private val userService: UserService,
    private val userPositionService: UserPositionService,
) {
    @Transactional
    fun createTrade(matchResult: MatchResult): Trade {
        val trade = Trade(
            yesOrderId = matchResult.yesOrder.id,
            noOrderId = matchResult.noOrder.id,
            pulseId = matchResult.yesOrder.pulseId,
            tradedQuantity = matchResult.matchedQuantity,
            tradedYesPrice = matchResult.matchedYesPrice,
            tradedNoPrice = BigDecimal.TEN.subtract(matchResult.matchedYesPrice),
            matchId = matchResult.yesOrder.matchId,
            createdAt = Instant.now()
        )
        val savedTrade = tradeRepository.save(trade)

        updateUserBalances(matchResult)
        updateUserPositions(matchResult)

        return savedTrade
    }

    private fun updateUserBalances(matchResult: MatchResult) {
        val yesTradeAmount = matchResult.matchedYesPrice.multiply(BigDecimal(matchResult.matchedQuantity))
        val noTradeAmount = matchResult.matchedNoPrice.multiply(BigDecimal(matchResult.matchedQuantity))

        // Deduct from the Yes buyer's wallet
        userService.confirmReservedBalance(matchResult.yesOrder.userId, yesTradeAmount)

        // Deduct from the No buyer's wallet
        userService.confirmReservedBalance(matchResult.noOrder.userId, noTradeAmount)
    }

    private fun updateUserPositions(matchResult: MatchResult) {
        // Yes position
        userPositionService.updatePosition(
            matchResult.yesOrder.userId,
            matchResult.yesOrder.pulseId,
            OrderSide.Yes,
            matchResult.matchedQuantity,
            matchResult.matchedYesPrice
        )
        // No position
        userPositionService.updatePosition(
            matchResult.noOrder.userId,
            matchResult.noOrder.pulseId,
            OrderSide.No,
            matchResult.matchedQuantity, // no order is also buy order hence adding the quantity
            matchResult.matchedNoPrice
        )
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
        return tradeRepository.findByYesOrderIdOrNoOrderId(orderId, orderId)
    }

    // Get the most recent trades for a pulse ID (e.g., last 10 trades)
    fun getRecentTradesByPulse(pulseId: Int, limit: Int): List<Trade> {
        return tradeRepository.findTopByPulseIdOrderByCreatedAtDesc(pulseId, limit) ?: emptyList()
    }

    // Get trades within a date range for a pulse ID
    fun getTradesByPulseAndDateRange(pulseId: Int, startDate: Instant, endDate: Instant): List<Trade> {
        return tradeRepository.findByPulseIdAndCreatedAtBetween(pulseId, startDate, endDate)
    }

}
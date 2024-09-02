package com.hit11.zeus.service

import com.hit11.zeus.model.*
import com.hit11.zeus.repository.OrderRepository
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
            buyOrderId = matchResult.buyOrder.id,
            sellOrderId = matchResult.sellOrder.id,
            pulseId = matchResult.buyOrder.pulseId,
            quantity = matchResult.quantity,
            price = matchResult.price,
            matchId = matchResult.buyOrder.matchId,
            createdAt = Instant.now()
        )
        val savedTrade = tradeRepository.save(trade)

        updateUserBalances(matchResult)
        updateUserPositions(matchResult)

        return savedTrade
    }

    private fun updateUserBalances(matchResult: MatchResult) {
        val tradeAmount = matchResult.price.multiply(BigDecimal(matchResult.quantity))

        // For the buyer, confirm the reserved balance (deduct from wallet)
        userService.confirmReservedBalance(matchResult.buyOrder.userId, tradeAmount)

        // For the seller, credit their wallet
        userService.updateUserWallet(matchResult.sellOrder.userId, tradeAmount)
    }

    private fun updateUserPositions(matchResult: MatchResult) {
        userPositionService.updatePosition(
            matchResult.buyOrder.userId,
            matchResult.buyOrder.pulseId,
            matchResult.side,
            matchResult.quantity,
            matchResult.price
        )
        userPositionService.updatePosition(
            matchResult.sellOrder.userId,
            matchResult.sellOrder.pulseId,
            matchResult.side,
            -matchResult.quantity,
            matchResult.price
        )
    }

    fun getTradesByPulse(pulseId: Int): List<Trade> {
        return tradeRepository.findByPulseId(pulseId)
    }

    fun getTradesByOrder(orderId: Long): List<Trade> {
        return tradeRepository.findByBuyOrderIdOrSellOrderId(orderId, orderId)
    }

    fun getTradesByMatch(matchId: Int): List<Trade> {
        return tradeRepository.findByMatchId(matchId)
    }
}
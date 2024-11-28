package com.hit11.zeus.service

import com.hit11.zeus.model.MatchedOrderEntity
import com.hit11.zeus.model.Order
import com.hit11.zeus.model.OrderSide
import com.hit11.zeus.repository.MatchedOrderRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import javax.transaction.Transactional

@Service
class OrderExecutionService(
    private val matchedOrderRepository: MatchedOrderRepository,
    private val tradeService: TradeService,
    private val userService: UserService,
    private val userPositionService: UserPositionService
) {
    @Transactional
    fun processMatches(order: Order, matches: List<OrderMatch>) {
        matches.forEach { match ->
            val execution = createOrderExecution(match)
            matchedOrderRepository.save(execution)
            tradeService.createTrade(match)
            updateUserBalances(match)
            updateUserPositions(match)
        }
    }

    private fun createOrderExecution(match: OrderMatch) = MatchedOrderEntity(
        yesOrderId = match.yesOrder.id,
        noOrderId = match.noOrder.id,
        pulseId = match.yesOrder.pulseId,
        matchId = match.yesOrder.matchId,
        matchedQuantity = match.matchedQuantity,
        matchedYesPrice = match.matchedYesPrice,
        matchedNoPrice = match.matchedNoPrice

    )

    private fun updateUserBalances(orderMatch: OrderMatch) {
        val yesTradeAmount = orderMatch.matchedYesPrice.multiply(BigDecimal(orderMatch.matchedQuantity))
        val noTradeAmount = orderMatch.matchedNoPrice.multiply(BigDecimal(orderMatch.matchedQuantity))

        // Deduct from the Yes buyer's wallet
        userService.confirmReservedBalance(orderMatch.yesOrder.userId, yesTradeAmount)

        // Deduct from the No buyer's wallet
        userService.confirmReservedBalance(orderMatch.noOrder.userId, noTradeAmount)
    }

    private fun updateUserPositions(orderMatch: OrderMatch) {
        // Yes position
        userPositionService.updatePosition(
            orderMatch.yesOrder.userId,
            orderMatch.yesOrder.pulseId,
            orderMatch.yesOrder.matchId,
            OrderSide.Yes,
            orderMatch.matchedQuantity,
            orderMatch.matchedYesPrice
        )
        // No position
        userPositionService.updatePosition(
            orderMatch.noOrder.userId,
            orderMatch.noOrder.pulseId,
            orderMatch.noOrder.matchId,
            OrderSide.No,
            orderMatch.matchedQuantity, // no order is also buy order hence adding the quantity
            orderMatch.matchedNoPrice
        )
    }

    // Get all trades for a given pulse ID
    fun getMatchedOrdersByPulse(pulseId: Int): List<MatchedOrderEntity> {
        return matchedOrderRepository.findByPulseId(pulseId)
    }

    // Get all trades for a given match ID
    fun getMatchedOrdersByMatch(matchId: Int): List<MatchedOrderEntity> {
        return matchedOrderRepository.findByMatchId(matchId)
    }

    // Get all trades for a given order (yesOrderId or noOrderId)
    fun getMatchedOrdersByOrder(orderId: Long): List<MatchedOrderEntity> {
        return matchedOrderRepository.findByYesOrderIdOrNoOrderId(orderId, orderId)
    }
}
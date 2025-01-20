package com.hit11.zeus.service

import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.Order
import com.hit11.zeus.model.OrderExecution
import com.hit11.zeus.model.OrderSide
import com.hit11.zeus.repository.MatchedOrderRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
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
            // For sells/exits, we need average entry price from position
            val avgEntryPrice = if (!order.isBuyOrder) {
                userPositionService.getPositionsByUserAndPulse(
                    order.userId,
                    order.pulseId
                ).firstOrNull { it.orderSide == order.orderSide }?.averagePrice
                    ?: throw OrderValidationException("No position found for exit")
            } else null

            val execution = createOrderExecution(match)
            matchedOrderRepository.save(execution)
            tradeService.createTrade(match, avgEntryPrice)
            updateUserBalances(match)
            updateUserPositions(match)
        }
    }

    private fun createOrderExecution(match: OrderMatch) = OrderExecution(
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

        // Only deduct from buyer's wallets
        if (orderMatch.yesOrder.isBuyOrder) {
            userService.confirmReservedBalance(orderMatch.yesOrder.userId, yesTradeAmount)
        } else {
            // For sell (exit) orders, add funds to seller’s wallet
            userService.addBalance(orderMatch.yesOrder.userId, yesTradeAmount)
        }

        if (orderMatch.noOrder.isBuyOrder) {
            userService.confirmReservedBalance(orderMatch.noOrder.userId, noTradeAmount)
        } else {
            // For sell (exit) orders, add funds to seller’s wallet
            userService.addBalance(orderMatch.noOrder.userId, noTradeAmount)
        }
    }

    private fun updateUserPositions(orderMatch: OrderMatch) {
        // Yes position
        val yesQuantity = if (orderMatch.yesOrder.isBuyOrder)
            orderMatch.matchedQuantity
        else
            -orderMatch.matchedQuantity // Reduce for sell orders

        userPositionService.updateOrCreateUserPosition(
            orderMatch.yesOrder.userId,
            orderMatch.yesOrder.pulseId,
            orderMatch.yesOrder.matchId,
            OrderSide.Yes,
            yesQuantity,  // Positive for buy, negative for sell
            orderMatch.matchedYesPrice
        )

        // No position
        val noQuantity = if (orderMatch.noOrder.isBuyOrder)
            orderMatch.matchedQuantity
        else
            -orderMatch.matchedQuantity

        userPositionService.updateOrCreateUserPosition(
            orderMatch.noOrder.userId,
            orderMatch.noOrder.pulseId,
            orderMatch.noOrder.matchId,
            OrderSide.No,
            noQuantity,  // Positive for buy, negative for sell
            orderMatch.matchedNoPrice
        )
    }

    // Get all trades for a given pulse ID
    fun getMatchedOrdersByPulse(pulseId: Int): List<OrderExecution> {
        return matchedOrderRepository.findByPulseId(pulseId)
    }

    private val pulseIdToOrderExecution: HashMap<Int, OrderExecution> = HashMap()
    private val pulseIdToUpdatedAt: HashMap<Int, Instant> = HashMap()
    private val cacheMsTTL: Long = 1000L

    fun getLastTradedPulses(pulseIds: List<Int>): List<OrderExecution> {
        val orderExecutions = mutableListOf<OrderExecution>()
        for (pulseId in pulseIds) {
            val updatedAt = pulseIdToUpdatedAt[pulseId]
            val orderExecution = pulseIdToOrderExecution[pulseId]
            if (updatedAt != null && updatedAt.plusMillis(cacheMsTTL) < Instant.now() && orderExecution != null) {
                orderExecutions.add(orderExecution)
            } else {
                val lastOrderExecution = matchedOrderRepository.findTopByPulseIdOrderByCreatedAtDesc(pulseId)
                if (lastOrderExecution != null) {
                    pulseIdToOrderExecution[pulseId] = lastOrderExecution
                    pulseIdToUpdatedAt[pulseId] = Instant.now()
                    orderExecutions.add(lastOrderExecution)
                }
            }
        }
        return orderExecutions
    }

    // Get all trades for a given match ID
    fun getMatchedOrdersByMatch(matchId: Int): List<OrderExecution> {
        return matchedOrderRepository.findByMatchId(matchId)
    }

    // Get all trades for a given order (yesOrderId or noOrderId)
    fun getMatchedOrdersByOrder(orderId: Long): List<OrderExecution> {
        return matchedOrderRepository.findByYesOrderIdOrNoOrderId(orderId, orderId)
    }
}
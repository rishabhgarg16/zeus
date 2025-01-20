package com.hit11.zeus.service

import com.hit11.zeus.model.PulseResult
import com.hit11.zeus.model.Question
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class PayoutService (
    private val tradeService: TradeService,
    private val userPositionService: UserPositionService,
    private val orderService: OrderService
){
    @Transactional
    fun processPayouts(
        question: Question,
        pulseResult: PulseResult
    ) {
        try {
            // close all trades for this question
            tradeService.settleTradesByPulse(question.id, pulseResult)
            // Close all positions for the pulse
            userPositionService.closePulsePositions(question.id, pulseResult)

            // 3. Cancel all open orders and handle funds
            val openOrders = orderService.getOpenOrdersByPulse(question.id)
            openOrders.forEach { order ->
                // Only return funds for buy orders
                if (order.isBuyOrder) {
                    orderService.cancelOrder(order.id)
                } else {
                    // For sell orders, just cancel without returning funds
                    orderService.cancelOrderWithoutFundReturn(order.id)
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to process payouts for pulse ${question.id}: ${e.message}", e)
        }
    }
}
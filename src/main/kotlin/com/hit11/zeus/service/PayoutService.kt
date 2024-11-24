package com.hit11.zeus.service

import com.hit11.zeus.model.PulseResult
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.Trade
import com.hit11.zeus.model.TradeResult
import org.springframework.stereotype.Service

@Service
class PayoutService (
    private val userPositionService: UserPositionService,
    private val orderService: OrderService
){
    fun processPayouts(question: QuestionDataModel) {
        try {
            // Close all positions for the pulse
            userPositionService.closePulsePositions(question.id)

            // Cancel all open and partially filled orders
            orderService.cancelAllOpenOrders(question.id)
        } catch (e: Exception) {
            throw RuntimeException("Failed to process payouts for pulse ${question.id}: ${e.message}", e)
        }
    }
}
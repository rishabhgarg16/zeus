package com.hit11.zeus.service

import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.oms.OrderService
import com.hit11.zeus.oms.Trade
import com.hit11.zeus.oms.TradeResult
import com.hit11.zeus.oms.TradeService
import org.springframework.stereotype.Service

@Service
class PayoutService (
    private val tradeService: TradeService,
    private val orderService: OrderService,
    private val userService: UserService,
){
    fun processPayouts(question: QuestionDataModel) {
        val trades = tradeService.getTradesByQuestionId(question.id)
        trades?.forEach { trade ->
            if (trade.userAnswer == question.pulseResult) {
                trade.result = TradeResult.WIN
                val winnings = calculateWinnings(trade)
                userService.updateBalance(trade.userId, winnings)
            } else {
                trade.result = TradeResult.LOSE
            }
            tradeService.updateTrade(trade)
        }
        orderService.cancelAllOpenOrders(question.id)
    }

    private fun calculateWinnings(trade: Trade): Double {
        // Implement your winnings calculation logic here
        // This could be based on odds, stake amount, etc.
        return trade.tradeQuantity * 10.0 // Simple example: 10x the quantity
    }

}
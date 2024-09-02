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
        userPositionService.closePulsePositions(question.id)
        orderService.cancelAllOpenOrders(question.id)
    }
}
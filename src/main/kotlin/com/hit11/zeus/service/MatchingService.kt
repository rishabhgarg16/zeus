package com.hit11.zeus.service

import com.hit11.zeus.oms.OrderDataModel
import com.hit11.zeus.oms.OrderRepository
import com.hit11.zeus.oms.OrderState
import com.hit11.zeus.oms.toDataModel
import com.hit11.zeus.utils.Constants
import org.springframework.stereotype.Service

@Service
class MatchingService(
    private val orderRepository: OrderRepository
) {
    fun findMatch(order: OrderDataModel): OrderDataModel? {
        val oppositeAnswer = if (order.userAnswer == "YES") "NO" else "YES"
        val complementaryPrice = Constants.BIG_DECIMAL_TEN - order.price

        return orderRepository.findMatchingOrder(
            order.pulseId,
            oppositeAnswer,
            complementaryPrice,
            OrderState.OPEN,
            order.remainingQuantity
        )?.toDataModel()
    }

    fun calculateMatch(currentOrder: OrderDataModel, matchedOrder: OrderDataModel): MatchResult {
        val matchedQuantity = minOf(currentOrder.remainingQuantity, matchedOrder.remainingQuantity)

        val updatedOrder1 = currentOrder.copy(
            remainingQuantity = currentOrder.remainingQuantity - matchedQuantity,
            state = if (currentOrder.remainingQuantity == matchedQuantity) OrderState.FULLY_MATCHED else OrderState.PARTIALLY_MATCHED
        )

        val updatedOrder2 = matchedOrder.copy(
            remainingQuantity = matchedOrder.remainingQuantity - matchedQuantity,
            state = if (matchedOrder.remainingQuantity == matchedQuantity) OrderState.FULLY_MATCHED else OrderState.PARTIALLY_MATCHED
        )

        return MatchResult(updatedOrder1, updatedOrder2, matchedQuantity)
    }
}

data class MatchResult(
    val order1: OrderDataModel,
    val order2: OrderDataModel,
    val matchedQuantity: Long
)
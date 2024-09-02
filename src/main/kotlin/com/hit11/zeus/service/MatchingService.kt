//package com.hit11.zeus.service
//
//import com.hit11.zeus.model.Order
//import com.hit11.zeus.repository.OrderRepository
//import com.hit11.zeus.model.OrderState
//import com.hit11.zeus.utils.Constants
//import org.springframework.stereotype.Service
//
//@Service
//class MatchingService(
//    private val orderRepository: OrderRepository
//) {
//    fun findPotentialMatches(order: Order): List<Order> {
//        val oppositeAnswer = if (order.userAnswer == PulseResult.Yes) PulseResult.No else PulseResult.Yes
//        val complementaryPrice = Constants.BIG_DECIMAL_TEN - order.price
//
//        return orderRepository.findMatchingOrder(
//            order.pulseId,
//            oppositeAnswer,
//            complementaryPrice,
//            OrderState.OPEN,
//            order.remainingQuantity
//        )?.map{it.toDataModel()} ?: emptyList()
//    }
//
//    fun calculateMatch(currentOrder: Order, matchedOrder: Order): MatchResult {
//        val matchedQuantity = minOf(currentOrder.remainingQuantity, matchedOrder.remainingQuantity)
//
//        val updatedOrder1 = currentOrder.copy(
//            remainingQuantity = currentOrder.remainingQuantity - matchedQuantity,
//            state = if (currentOrder.remainingQuantity == matchedQuantity) OrderState.FULLY_MATCHED else OrderState.PARTIALLY_MATCHED
//        )
//
//        val updatedOrder2 = matchedOrder.copy(
//            remainingQuantity = matchedOrder.remainingQuantity - matchedQuantity,
//            state = if (matchedOrder.remainingQuantity == matchedQuantity) OrderState.FULLY_MATCHED else OrderState.PARTIALLY_MATCHED
//        )
//
//        return MatchResult(updatedOrder1, updatedOrder2, matchedQuantity)
//    }
//}
//
////data class MatchResult(
////    val order1: Order,
////    val order2: Order,
////    val matchedQuantity: Long
////)
package com.hit11.zeus.oms

import com.hit11.zeus.service.MatchingService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class OrderOrchestrator(
    private val orderService: OrderService,
    private val matchingService: MatchingService,
    private val tradeService: TradeService
) {
    private val logger = LoggerFactory.getLogger(OrderOrchestrator::class.java)

    @Transactional
    fun processOrder(newOrder: OrderDataModel) {
        var currentOrder = newOrder

//        while (currentOrder.remainingQuantity > 0) {
//            // TODO optimize this, single DB call instead of multiple
//            val matchedOrder = matchingService.findMatch(currentOrder)
//
//            if (matchedOrder != null) {
//                logger.info("Order ${currentOrder.id} matched with ${matchedOrder.id}")
//                val (updatedNew, updatedMatched) = processMatch(currentOrder, matchedOrder)
//                currentOrder = updatedNew
//            } else {
//                break
//            }
//        }
//
//        if (currentOrder.remainingQuantity > 0) {
//            logger.info("Saving remaining quantity ${currentOrder.remainingQuantity} for order ${newOrder.id}")
//            orderService.saveOrder(currentOrder)
//        }

         // TODO order processing logic
        orderService.createNewOrder(currentOrder)
        tradeService.createTrade(currentOrder, currentOrder.quantity)
    }

    private fun processMatch(
        currentOrder: OrderDataModel,
        matchedOrder: OrderDataModel
    ): Pair<OrderDataModel, OrderDataModel> {
        val matchResult = matchingService.calculateMatch(currentOrder, matchedOrder)

        val updatedOrder1 = orderService.saveOrUpdateOrder(matchResult.order1)
        val updatedOrder2 = orderService.saveOrUpdateOrder(matchResult.order2)

        tradeService.createTrade(updatedOrder1, matchResult.matchedQuantity)
        tradeService.createTrade(updatedOrder2, matchResult.matchedQuantity)

        return Pair(updatedOrder1, updatedOrder2)
    }
}
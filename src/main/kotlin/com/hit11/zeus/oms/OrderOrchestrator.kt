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

        // Check for matches until no more matches are found
        while (currentOrder.remainingQuantity > 0) {
            // Find all potential matches
            val potentialMatches = matchingService.findPotentialMatches(currentOrder)

            // If no potential matches, save the current order and exit loop
            if (potentialMatches.isEmpty()) {
                orderService.saveOrder(currentOrder)
                break
            }

            // Process all potential matches
            for (matchedOrder in potentialMatches) {
                if (currentOrder.remainingQuantity <= 0) break

                val (updatedNew, updatedMatched) = processMatch(currentOrder, matchedOrder)
                currentOrder = updatedNew

                // Save the updated matched order
                orderService.saveOrUpdateOrder(updatedMatched)
            }
        }

        // Save the current order if it has remaining quantity
        if (currentOrder.remainingQuantity > 0) {
            orderService.saveOrder(currentOrder)
        }
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
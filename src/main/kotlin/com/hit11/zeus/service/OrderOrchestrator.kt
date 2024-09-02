//package com.hit11.zeus.service
//
//import com.hit11.zeus.exception.OrderValidationException
//import com.hit11.zeus.model.MatchStatus
//import com.hit11.zeus.model.QuestionDataModel
//import com.hit11.zeus.model.QuestionStatus
//import com.hit11.zeus.model.Order
//import com.hit11.zeus.model.OrderType
//import com.hit11.zeus.repository.MatchRepository
//import com.hit11.zeus.repository.QuestionRepository
//import com.hit11.zeus.repository.UserRepository
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Service
//import java.math.BigDecimal
//import java.time.Instant
//import javax.transaction.Transactional
//
//@Service
//class OrderOrchestrator(
//    private val orderService: OrderService,
//    private val matchingService: MatchingService,
//    private val tradeService: TradeService,
//    private val questionRepository: QuestionRepository,
//    private val matchRepository: MatchRepository,
//    private val userRepository: UserRepository
//) {
//    private val logger = LoggerFactory.getLogger(OrderOrchestrator::class.java)
//
//    @Transactional
//    fun processOrder(newOrder: Order) {
//        validateOrder(newOrder)
//        var currentOrder = newOrder
//
////        while (currentOrder.remainingQuantity > 0) {
////            // TODO optimize this, single DB call instead of multiple
////            val matchedOrder = matchingService.findMatch(currentOrder)
////
////            if (matchedOrder != null) {
////                logger.info("Order ${currentOrder.id} matched with ${matchedOrder.id}")
////                val (updatedNew, updatedMatched) = processMatch(currentOrder, matchedOrder)
////                currentOrder = updatedNew
////            } else {
////                break
////            }
////        }
////
////        if (currentOrder.remainingQuantity > 0) {
////            logger.info("Saving remaining quantity ${currentOrder.remainingQuantity} for order ${newOrder.id}")
////            orderService.saveOrder(currentOrder)
////        }
//
//        // TODO order processing logic
//        val createdOrder = orderService.createNewOrder(currentOrder)
//        tradeService.createTrade(createdOrder, createdOrder.quantity)
//    }
//
////    @Transactional
////    fun processOrder(newOrder: OrderDataModel) {
////        var currentOrder = newOrder
////
////        // Check for matches until no more matches are found
////        while (currentOrder.remainingQuantity > 0) {
////            // Find all potential matches
////            val potentialMatches = matchingService.findPotentialMatches(currentOrder)
////
////            // If no potential matches, save the current order and exit loop
////            if (potentialMatches.isEmpty()) {
////                orderService.saveOrder(currentOrder)
////                break
////            }
////
////            // Process all potential matches
////            for (matchedOrder in potentialMatches) {
////                if (currentOrder.remainingQuantity <= 0) break
////
////                val (updatedNew, updatedMatched) = processMatch(currentOrder, matchedOrder)
////                currentOrder = updatedNew
////
////                // Save the updated matched order
////                orderService.saveOrUpdateOrder(updatedMatched)
////            }
////        }
////
////        // Save the current order if it has remaining quantity
////        if (currentOrder.remainingQuantity > 0) {
////            orderService.saveOrder(currentOrder)
////        }
////    }
//
//    private fun processMatch(currentOrder: Order, matchedOrder: Order): Pair<Order, Order> {
//        val matchResult = matchingService.calculateMatch(currentOrder, matchedOrder)
//
//        val updatedOrder1 = orderService.saveOrUpdateOrder(matchResult.order1)
//        val updatedOrder2 = orderService.saveOrUpdateOrder(matchResult.order2)
//
//        tradeService.createTrade(updatedOrder1, matchResult.matchedQuantity)
//        tradeService.createTrade(updatedOrder2, matchResult.matchedQuantity)
//
//        return Pair(updatedOrder1, updatedOrder2)
//    }
//
//
//}
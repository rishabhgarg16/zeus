package com.hit11.zeus.oms

import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.MatchStatus
import com.hit11.zeus.model.QuestionStatus
import com.hit11.zeus.repository.MatchRepository
import com.hit11.zeus.repository.QuestionRepository
import com.hit11.zeus.repository.UserRepository
import com.hit11.zeus.service.MatchingService
import com.hit11.zeus.utils.Constants
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import javax.transaction.Transactional

@Service
class OrderOrchestrator(
    private val orderService: OrderService,
    private val orderRepository: OrderRepository,
    private val matchingService: MatchingService,
    private val tradeService: TradeService,
    private val questionRepository: QuestionRepository,
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(OrderOrchestrator::class.java)

    @Transactional
    fun processOrder(newOrder: OrderDataModel) {
        validateOrder(newOrder)
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
        val createdOrder = orderService.createNewOrder(currentOrder)
        tradeService.createTrade(createdOrder, createdOrder.quantity)
    }

//    @Transactional
//    fun processOrder(newOrder: OrderDataModel) {
//        var currentOrder = newOrder
//
//        // Check for matches until no more matches are found
//        while (currentOrder.remainingQuantity > 0) {
//            // Find all potential matches
//            val potentialMatches = matchingService.findPotentialMatches(currentOrder)
//
//            // If no potential matches, save the current order and exit loop
//            if (potentialMatches.isEmpty()) {
//                orderService.saveOrder(currentOrder)
//                break
//            }
//
//            // Process all potential matches
//            for (matchedOrder in potentialMatches) {
//                if (currentOrder.remainingQuantity <= 0) break
//
//                val (updatedNew, updatedMatched) = processMatch(currentOrder, matchedOrder)
//                currentOrder = updatedNew
//
//                // Save the updated matched order
//                orderService.saveOrUpdateOrder(updatedMatched)
//            }
//        }
//
//        // Save the current order if it has remaining quantity
//        if (currentOrder.remainingQuantity > 0) {
//            orderService.saveOrder(currentOrder)
//        }
//    }

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

    private fun validateOrder(order: OrderDataModel) {
        val match = matchRepository.findById(order.matchId)
            .orElseThrow { OrderValidationException("Match not found") }

        // Check if match is still ongoing
        if (match.status == MatchStatus.COMPLETE.text) {
            throw OrderValidationException("Match has ended")
        }

        val question = questionRepository.findById(order.pulseId)
            .orElseThrow { OrderValidationException("Question not found") }

        // Check question status
        if (question.status != QuestionStatus.LIVE) {
            throw OrderValidationException("Question is not active")
        }

        // Validate user
        val user = userRepository.findById(order.userId)
            .orElseThrow { OrderValidationException("User not found") }

        // Check user balance
        val orderTotal = order.price.multiply(BigDecimal(order.quantity))
        if (user.walletBalance < orderTotal) {
            throw OrderValidationException("Insufficient balance")
        }

        // Validate wager
        when (order.userAnswer) {
            question.optionA -> {
                if (order.price != question.optionAWager.toBigDecimal()
                        .setScale(
                            Constants.DEFAULT_SCALE,
                            Constants.ROUNDING_MODE
                        )
                ) {
                    throw OrderValidationException("Invalid wager for Option A")
                }
            }

            question.optionB -> {
                if (order.price !=
                    question.optionBWager.toBigDecimal()
                        .setScale(
                            Constants.DEFAULT_SCALE,
                            Constants.ROUNDING_MODE
                        )
                ) {
                    throw OrderValidationException("Invalid wager for Option B")
                }
            }

            else -> throw OrderValidationException("Invalid answer option")
        }

        // Check if the order quantity is within allowed limits
        val maxAllowedQuantity = 1000L
        if (order.quantity > maxAllowedQuantity) {
            throw OrderValidationException("Order quantity exceeds maximum allowed")
        }

        // Additional time-based validation
        val currentTime = Instant.now()
        if (order.answerTime < currentTime.minusSeconds(120) ||
            order.answerTime > currentTime.plusSeconds(120)
        ) {
            throw OrderValidationException("Order time is out of acceptable range")
        }
    }
}
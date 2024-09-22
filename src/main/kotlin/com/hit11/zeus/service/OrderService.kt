package com.hit11.zeus.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hit11.zeus.config.AwsProperties
import com.hit11.zeus.exception.*
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.MatchRepository
import com.hit11.zeus.repository.OrderRepository
import com.hit11.zeus.repository.QuestionRepository
import com.hit11.zeus.repository.UserRepository
import com.hit11.zeus.utils.Constants
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.math.BigDecimal
import java.time.Instant
import javax.transaction.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val matchRepository: MatchRepository,
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val awsProperties: AwsProperties,
    private val sqsClient: SqsClient,
    private val matchingEngine: MatchingEngine,
    private val tradeService: TradeService,
    private val objectMapper: ObjectMapper
) {
    private val logger = Logger.getLogger(this.javaClass)

    @Transactional
    fun createOrder(orderRequest: OrderRequest): Order {
        validateOrder(orderRequest)
        try {
            val order = Order(
                userId = orderRequest.userId,
                pulseId = orderRequest.pulseId,
                matchId = orderRequest.matchId,
                orderType = orderRequest.orderType,
                orderSide = if(orderRequest.userAnswer == OrderSide.No.name) { OrderSide.No} else OrderSide.Yes,
                price = orderRequest.price.toBigDecimal()
                    .setScale(Constants.DEFAULT_SCALE, Constants.ROUNDING_MODE),
                quantity = orderRequest.quantity,
                remainingQuantity = orderRequest.quantity,
                createdAt = Instant.now(),
                executionType = orderRequest.executionType
            )

            if (order.orderType == OrderType.BUY) {
                val reserveAmount = order.price.multiply(BigDecimal(order.quantity))
                if (!userService.reserveBalance(order.userId, reserveAmount)) {
                    throw InsufficientBalanceException("Insufficient balance for user ${order.userId}")
                }
            }

            val savedOrder = orderRepository.save(order)

            val matches = matchingEngine.processOrder(savedOrder)
            processTrades(matches)

            return orderRepository.save(savedOrder)
        } catch (e: Exception) {
            logger.error("Error saving order for user id ${orderRequest.userId}", e)
            throw OrderCreationException("Not able to save order for User ${orderRequest.userId}")
        }
    }

    private fun processTrades(matches: List<MatchResult>) {
        matches.forEach { match ->
            tradeService.createTrade(match)
            updateOrderStatus(match.buyOrder)
            updateOrderStatus(match.sellOrder)
        }
    }

    private fun updateOrderStatus(order: Order) {
        when (order.remainingQuantity) {
            0L -> order.status = OrderStatus.FILLED
            in 1..order.quantity -> order.status = OrderStatus.PARTIALLY_FILLED
        }
        orderRepository.save(order)
    }

    @Transactional
    fun cancelOrder(orderId: Int): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found") }

        if (order.status != OrderStatus.OPEN && order.status != OrderStatus.PARTIALLY_FILLED) {
            throw IllegalStateException("Cannot cancel order in state ${order.status}")
        }

        matchingEngine.cancelOrder(order)
        order.status = OrderStatus.CANCELLED

        if (order.orderType == OrderType.BUY) {
            val releaseAmount = order.price.multiply(BigDecimal(order.remainingQuantity))
            userService.releaseReservedBalance(order.userId, releaseAmount)
        }

        return orderRepository.save(order)
    }


    @Transactional
    fun cancelAllOpenOrders(questionId: Int) {
        val openOrders = orderRepository.findByPulseIdAndStatus(questionId, OrderStatus.OPEN)
        openOrders.forEach { order ->
            matchingEngine.cancelOrder(order)
            order.status = OrderStatus.CANCELLED
            if (order.orderType == OrderType.BUY) {
                val releaseAmount = order.price.multiply(BigDecimal(order.remainingQuantity))
                userService.releaseReservedBalance(order.userId, releaseAmount)
            }
            orderRepository.save(order)
        }
    }

    private fun releaseReservedBalance(order: Order) {
        val releaseAmount = order.price.multiply(BigDecimal(order.remainingQuantity))
        userService.releaseReservedBalance(order.userId, releaseAmount)
    }

    private fun reserveBalance(order: Order) {
        val requiredBalance = order.price.multiply(BigDecimal(order.quantity))
        userService.reserveBalance(order.userId, requiredBalance)
    }

//    @Transactional
//    fun saveOrUpdateOrder(order: Order): Order {
//        val existingOrder = orderRepository.findById(order.id)
//        return if (existingOrder.isPresent) {
//            val updated = existingOrder.get().copy(
//                remainingQuantity = order.remainingQuantity,
//                status = order.status
//            )
//            orderRepository.save(updated)
//        } else {
//            saveOrder(order)
//        }
//    }

//    @Transactional
//    fun saveOrder(order: Order): Order {
//        try {
//            val entity = Order(
//                userId = order.userId,
//                pulseId = order.pulseId,
//                matchId = order.matchId,
//                userAnswer = order.userAnswer,
//                price = order.price,
//                quantity = order.quantity,
//                remainingQuantity = 0, // TODO Change it later
//                status = OrderStatus.FILLED // TODO change it later
//            )
//            return orderRepository.save(entity)
//        } catch (e: Exception) {
//            logger.error("Error saving order for user id ${order.userId}", e)
//            throw OrderNotSaveException("Not able to save order for User ${order.userId}")
//        }
//    }

//    fun getAllTradesByPulseId(pulseId: Int): List<Order>? {
//        return orderRepository.findTradesByPulseId(pulseId)
//    }

    fun getOpenOrdersByUser(userId: Int): List<Order> {
        return orderRepository.findByUserIdAndStatus(userId, OrderStatus.OPEN)
    }

    fun getOpenOrdersByPulse(pulseId: Int): List<Order> {
        return orderRepository.findByPulseIdAndStatus(pulseId, OrderStatus.OPEN)
    }

//    @Transactional
//    fun createNewOrder(order: Order): Order {
//        val amountToDeduct = "%.2f".format(order.totalAmount).toDouble()
//        val balanceSuccess = userService.updateBalance(order.userId, -amountToDeduct)
//
//        if (!balanceSuccess) {
//            logger.error("Error updating the user wallet for user id ${order.userId}")
//            throw InsufficientBalanceException("Insufficient balance for user ${order.userId}")
//        }
//
//        return saveOrder(order)
//    }

    fun sendOrderToQueue(order: Order) {
        try {
            val messageBody = objectMapper.writeValueAsString(order)
            val sendRequest = SendMessageRequest.builder()
                .queueUrl(awsProperties.sqs.queueUrl)
                .messageBody(messageBody)
                .build()
            sqsClient.sendMessage(sendRequest)
        } catch (e: Exception) {
            logger.error("Error sending order to queue for order id ${order.id}", e)
        }
    }

    @Transactional
    fun updateOrderStatus(orderId: Int, newStatus: OrderStatus) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException("Order not found with id: $orderId") }
        validateStateTransition(order.status, newStatus)
        order.status = newStatus
        handleOrderStateChange(order, newStatus)
        orderRepository.save(order)
        if (newStatus == OrderStatus.OPEN) {
            sendOrderToQueue(order)
        }
    }

    private fun handleOrderStateChange(order: Order, newStatus: OrderStatus) {
        when (newStatus) {
            OrderStatus.FILLED -> order.remainingQuantity = order.quantity
            OrderStatus.CANCELLED, OrderStatus.EXPIRED -> {
                // Handle fund return logic here
            }

            else -> {} // No action needed for other states
        }
    }

    private fun validateStateTransition(currentState: OrderStatus, newState: OrderStatus) {
        val validTransitions = mapOf(
            OrderStatus.OPEN to setOf(
                OrderStatus.PARTIALLY_FILLED,
                OrderStatus.FILLED,
                OrderStatus.CANCELLED,
                OrderStatus.EXPIRED
            ),
            OrderStatus.FILLED to setOf(
                OrderStatus.PARTIALLY_FILLED,
                OrderStatus.CANCELLED,
                OrderStatus.EXPIRED
            ),
            OrderStatus.FILLED to emptySet(),
            OrderStatus.CANCELLED to emptySet(),
            OrderStatus.EXPIRED to emptySet()
        )
        if (newState !in (validTransitions[currentState] ?: emptySet())) {
            throw IllegalStateException("Invalid state transition from $currentState to $newState")
        }
    }

    private fun validateOrder(order: OrderRequest) {
        val match = matchRepository.findActiveMatchById(order.matchId)
            .orElseThrow { OrderValidationException("Match not found") }

        if (match.status == MatchStatus.COMPLETE.text) {
            throw OrderValidationException("Match has ended")
        }

        val question = questionRepository.findById(order.pulseId)
            .orElseThrow { OrderValidationException("Question not found") }

        if (question.status != QuestionStatus.LIVE) {
            throw OrderValidationException("Question is not active")
        }

        val user = userRepository.findById(order.userId)
            .orElseThrow { OrderValidationException("User not found") }

        val orderTotal = order.price * order.quantity
        if (user.walletBalance.toDouble() < orderTotal) {
            throw OrderValidationException("Insufficient balance")
        }

        validateWager(order, question.mapToQuestionDataModel())
        validateQuantity(order)
        validateOrderTime(order)
    }

    private fun validateWager(order: OrderRequest, question: QuestionDataModel) {
        when (order.userAnswer) {
            question.optionA -> {
                if (order.price <= question.optionAWager.toDouble()) {
                    throw OrderValidationException("Invalid wager for Option A")
                }
            }

            question.optionB -> {
                if (order.price <= question.optionBWager.toDouble())
                    throw OrderValidationException("Invalid wager for Option B")
            }

            else -> throw OrderValidationException("Invalid answer option")
        }
    }

    private fun validateQuantity(order: OrderRequest) {
        val maxAllowedQuantity = 1000L
        if (order.quantity > maxAllowedQuantity) {
            throw OrderValidationException("Order quantity exceeds maximum allowed")
        }
    }

    private fun validateOrderTime(order: OrderRequest) {
        val currentTime = Instant.now()

        if (order.getCreatedAtAsInstant() < currentTime.minusSeconds(12000) ||
            order.getCreatedAtAsInstant() > currentTime.plusSeconds(12000)
        ) {
            throw OrderValidationException("Order time is out of acceptable range")
        }
    }
}
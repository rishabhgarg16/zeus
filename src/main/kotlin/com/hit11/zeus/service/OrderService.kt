package com.hit11.zeus.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hit11.zeus.config.AwsProperties
import com.hit11.zeus.exception.*
import com.hit11.zeus.model.*
import com.hit11.zeus.notification.NotificationService
import com.hit11.zeus.repository.MatchRepository
import com.hit11.zeus.repository.OrderRepository
import com.hit11.zeus.repository.QuestionRepository
import com.hit11.zeus.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import javax.annotation.PostConstruct
import javax.transaction.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val matchRepository: MatchRepository,
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val userPositionService: UserPositionService,
    private val awsProperties: AwsProperties,
    private val sqsClient: SqsClient,
    private val matchingEngine: MatchingEngine,
    private val orderExecutionService: OrderExecutionService,
    private val objectMapper: ObjectMapper,
    private val notificationService: NotificationService
) {
    private val logger = Logger.getLogger(this.javaClass)

    @PostConstruct
    fun initializeOrderBookOnStartup() {
        initializeOrderBook()
    }

    fun initializeOrderBook() {
        val openOrders = orderRepository.findAllByStatusIn(
            listOf(
                OrderStatus.OPEN,
                OrderStatus.PARTIALLY_FILLED
            )
        )
        openOrders.forEach { order ->
            val isAdded = matchingEngine.addOrder(order)
            if (!isAdded) {
                logger.warn("Order ${order.id} already exists in the queue. Skipping re-addition.")
            }
        }
    }

    @Transactional
    fun createOrder(orderRequest: OrderRequest): Boolean {
        validateOrder(orderRequest)
        try {
            val order = createInitialOrder(orderRequest)
            val savedOrder = orderRepository.save(order)


            // Only reserve balance for buy orders
            if (order.isBuyOrder) {
                val reserveAmount = savedOrder.price.multiply(BigDecimal(savedOrder.quantity))
                if (!userService.reserveBalance(savedOrder.userId, reserveAmount)) {
                    throw InsufficientBalanceException("Insufficient balance for user ${savedOrder.userId}")
                }
            }

            // 3. Find potential matches
            val matches = synchronized(matchingEngine) {
                matchingEngine.addOrder(savedOrder)  // Add to main queue first
                matchingEngine.findMatches(savedOrder)  // Then find matches
            }

            // 4. Process matches and create trades atomically
            if (matches.isNotEmpty()) {
                processMatchesAndTrades(savedOrder, matches)
            }

            return true

//            return orderRepository.save(savedOrder)
        } catch (e: Exception) {
            logger.error("Error processing order", e)
            handleOrderCreationFailure(orderRequest)
            throw e
        }
    }

    private fun createInitialOrder(request: OrderRequest): Order {
        // First fetch the related entities
        val match = matchRepository.findById(request.matchId)
            .orElseThrow { OrderValidationException("Match not found matchId ${request.matchId}") }

        val question = questionRepository.findById(request.pulseId)
            .orElseThrow { OrderValidationException("Question not found questionId ${request.pulseId}") }
        return Order(
            userId = request.userId,
            pulseId = request.pulseId,
            pulse = question,
            match = match,
            matchId = request.matchId,
            orderType = request.orderType,
            orderSide = if (request.userAnswer == OrderSide.No.name) OrderSide.No else OrderSide.Yes,
            price = request.price.toBigDecimal().setScale(2, RoundingMode.HALF_UP),
            quantity = request.quantity,
            remainingQuantity = request.quantity,
            createdAt = Instant.now(),
            executionType = request.executionType,
            status = OrderStatus.OPEN
        )
    }

    @Transactional
    private fun processMatchesAndTrades(order: Order, matches: List<OrderMatch>) {
        try {
            // Step 1: Create order match, create trades, updates positions and balance
            orderExecutionService.processMatches(order, matches)

            // Step 2: Confirm matches in the order book
            matchingEngine.confirmMatches(order, matches)

            // Step 3: Update statuses for all matched orders (after successful matches)
            matches.forEach { match ->
                updateOrderStatus(match.yesOrder)
                updateOrderStatus(match.noOrder)
            }

            // Step 4: Launch async pulse question update
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    matches.groupBy { it.yesOrder.pulseId }.forEach {
                        questionRepository.updateOptionWagers(
                            it.key,
                            it.value.last().matchedYesPrice,
                            it.value.last().matchedYesPrice
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Failed to update pulse questions", e)
                }
            }

        } catch (e: Exception) {
            logger.error("Failed to process matches and trades", e)
            throw OrderProcessingException("Failed to process matches and trades", e)
        }
    }

    private fun handleOrderCreationFailure(orderRequest: OrderRequest) {
        try {
            val reserveAmount = orderRequest.price.toBigDecimal()
                .multiply(BigDecimal(orderRequest.quantity))
            userService.releaseReservedBalance(orderRequest.userId, reserveAmount)
        } catch (e: Exception) {
            logger.error("Failed to handle order creation failure", e)
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
    fun cancelOrder(orderId: Long): Order {
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
        val savedOrder = orderRepository.save(order)

        // Send notification asynchronously
        notificationService.notifyOrderCancelled(savedOrder)

        return savedOrder
    }

    @Transactional
    fun cancelAllOpenOrders(pulseId: Int) {
        val openOrPartiallyFilledOrders = orderRepository.findByPulseIdAndStatusIn(
            pulseId, listOf(OrderStatus.OPEN, OrderStatus.PARTIALLY_FILLED)
        )
        val updatedOrders = openOrPartiallyFilledOrders.map { order ->
            try {
                matchingEngine.cancelOrder(order)

                // Only return funds for buy orders
                if (order.isBuyOrder) {
                    val releaseAmount = order.price.multiply(BigDecimal(order.remainingQuantity))
                    userService.releaseReservedBalance(order.userId, releaseAmount)
                }

                order.apply {
                    status = OrderStatus.CANCELLED
                }
            } catch (e: Exception) {
                logger.error("Error cancelling order ${order.id}: ${e.message}", e)
                throw e
            }
        }

        val savedOrders = orderRepository.saveAll(updatedOrders)
        savedOrders.forEach { order ->
            notificationService.notifyOrderCancelled(order)
        }
        logger.info("Successfully cancelled ${updatedOrders.size} orders for pulseId $pulseId")
    }

    @Transactional
    fun bulkCancelOrders(orderIds: List<Long>): Boolean {
        try {
            val orders = orderRepository.findAllById(orderIds)

            // Filter orders that can be cancelled
            val cancellableOrders = orders.filter { order ->
                order.status == OrderStatus.OPEN || order.status == OrderStatus.PARTIALLY_FILLED
            }

            // Cancel each order
            cancellableOrders.forEach { order ->
                matchingEngine.cancelOrder(order)
                order.status = OrderStatus.CANCELLED

                // Handle fund return logic
                if (order.orderType == OrderType.BUY) {
                    val releaseAmount = order.price.multiply(BigDecimal(order.remainingQuantity))
                    userService.releaseReservedBalance(order.userId, releaseAmount)
                }
            }

            // Save all cancelled orders
            val savedOrders = orderRepository.saveAll(cancellableOrders)
            savedOrders.forEach { order -> notificationService.notifyOrderCancelled(order) }

            return true
        } catch (e: Exception) {
            logger.error("Failed to cancel orders in bulk", e)
            throw e
        }
    }

    fun getPendingOrdersByUserIdAndPulseId(userId: Int, pulseId: Int): List<Order> {
        return orderRepository.findByUserIdAndPulseIdAndStatusIn(
            userId, pulseId, listOf(
                OrderStatus.OPEN,
                OrderStatus.PARTIALLY_FILLED
            )
        )
    }

    fun getPendingOrdersByUserIdAndMatchIds(
        userId: Int,
        matchIds: List<Int>
    ): List<UiOrderResponse> {
        val orders = orderRepository.findPendingOrdersWithDetails(
            userId,
            matchIds,
            listOf(OrderStatus.OPEN, OrderStatus.PARTIALLY_FILLED)
        )


        return orders.mapNotNull { order ->
            try {
                order.toUiOrderResponse()
            } catch (e: Exception) {
                logger.error("Error mapping order ${order.id} to UI response", e)
                null
            }
        }
    }


    fun getOpenOrdersByPulse(pulseId: Int): List<Order> {
        return orderRepository.findByPulseIdAndStatus(pulseId, OrderStatus.OPEN)
    }

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
    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
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

    private fun validateOrder(orderRequest: OrderRequest) {
        val match = matchRepository.findActiveMatchById(orderRequest.matchId)
            .orElseThrow { OrderValidationException("Match not found") }

        if (match.status == MatchStatus.COMPLETE.text) {
            throw OrderValidationException("Match has ended")
        }

        val question = questionRepository.findById(orderRequest.pulseId)
            .orElseThrow { OrderValidationException("Question not found") }

        if (question.status != QuestionStatus.LIVE) {
            throw OrderValidationException("Question is not active")
        }

        val user = userRepository.findById(orderRequest.userId)
            .orElseThrow { OrderValidationException("User not found") }

        if (orderRequest.isExitOrder) {
            // For sell-exit orders, validate position exists
            val positions = userPositionService.getPositionsByUserAndPulse(
                orderRequest.userId,
                orderRequest.pulseId
            ).filter { it.status == PositionStatus.OPEN }

            val positionToExit = positions.firstOrNull {
                it.orderSide.name == orderRequest.userAnswer
            } ?: throw OrderValidationException("No open position to exit")

            if (positionToExit.quantity < orderRequest.quantity) {
                throw OrderValidationException("Exit quantity exceeds open position")
            }
        } else {
            // For buy orders, validate wallet balance
            val orderTotal = orderRequest.price * orderRequest.quantity
            if (user.availableForTrading.toDouble() < orderTotal) {
                throw OrderValidationException("Insufficient balance")
            }
        }

        validateWager(orderRequest, question)
        validateQuantity(orderRequest)
        validateOrderTime(orderRequest)
    }

    private fun validateWager(order: OrderRequest, question: Question) {
        val MIN_WAGER = BigDecimal("0.5")
        val MAX_WAGER = BigDecimal("9.5")
        val WAGER_INCREMENT = BigDecimal("0.1")

        // Validate price is within global bounds
        val orderPrice = BigDecimal(order.price.toString())

        if (orderPrice < MIN_WAGER || orderPrice > MAX_WAGER) {
            throw OrderValidationException("Wager must be between ₹$MIN_WAGER and ₹$MAX_WAGER")
        }

        // Validate price follows increment
        if (orderPrice.remainder(WAGER_INCREMENT).setScale(0, RoundingMode.DOWN) != BigDecimal.ZERO) {
            throw OrderValidationException("Wager must be in increments of ₹$WAGER_INCREMENT")
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

    fun lastTradedPrices(pulseIds: List<Int>): List<OrderExecution> {
        return orderExecutionService.getLastTradedPulses(pulseIds)
    }

    @Transactional
    fun cancelOrderWithoutFundReturn(orderId: Long): Order {
        val order = orderRepository.findById(orderId)
            .orElseThrow { IllegalArgumentException("Order not found") }

        if (order.status != OrderStatus.OPEN && order.status != OrderStatus.PARTIALLY_FILLED) {
            throw IllegalStateException("Cannot cancel order in state ${order.status}")
        }

        // Remove from matching engine
        matchingEngine.cancelOrder(order)

        // Update status
        order.status = OrderStatus.CANCELLED

        val savedOrder = orderRepository.save(order)

        // Send notification asynchronously
        notificationService.notifyOrderCancelled(savedOrder)

        return savedOrder
    }
}
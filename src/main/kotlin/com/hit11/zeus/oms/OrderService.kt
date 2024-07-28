package com.hit11.zeus.oms

import com.fasterxml.jackson.databind.ObjectMapper
import com.hit11.zeus.config.AwsProperties
import com.hit11.zeus.exception.InsufficientBalanceException
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.OrderNotFoundException
import com.hit11.zeus.exception.OrderNotSaveException
import com.hit11.zeus.service.UserService
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import javax.transaction.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val userService: UserService,
    private val awsProperties: AwsProperties,
    private val sqsClient: SqsClient,
    private val objectMapper: ObjectMapper
) {
    private val logger = Logger.getLogger(this.javaClass)

    @Transactional
    fun saveOrUpdateOrder(order: OrderDataModel): OrderDataModel {
        val existingOrder = orderRepository.findById(order.id)
        return if (existingOrder.isPresent) {
            val updated = existingOrder.get().copy(
                remainingQuantity = order.remainingQuantity,
                state = order.state
            )
            orderRepository.save(updated).toDataModel()
        } else {
            saveOrder(order)
        }
    }

    @Transactional
    fun saveOrder(order: OrderDataModel): OrderDataModel {
        try {
            val entity = OrderEntity(
                userId = order.userId,
                pulseId = order.pulseId,
                matchId = order.matchId,
                userAnswer = order.userAnswer,
                price = order.price,
                quantity = order.quantity,
                remainingQuantity = 0, // TODO Change it later
                state = OrderState.FULLY_MATCHED // TODO change it later
            )
            return orderRepository.save(entity).toDataModel()
        } catch (e: Exception) {
            logger.error("Error saving order for user id ${order.userId}", e)
            throw OrderNotSaveException("Not able to save order for User ${order.userId}")
        }
    }

    fun getAllTradesByPulseId(pulseId: Int): List<OrderDataModel>? {
        return orderRepository.findTradesByPulseId(pulseId).map { it.toDataModel() }
    }

    @Transactional
    fun createNewOrder(order: OrderDataModel): OrderDataModel {
        val amountToDeduct = "%.2f".format(order.totalAmount).toDouble()
        val balanceSuccess = userService.updateBalance(order.userId, -amountToDeduct)

        if (!balanceSuccess) {
            logger.error("Error updating the user wallet for user id ${order.userId}")
            throw InsufficientBalanceException("Insufficient balance for user ${order.userId}")
        }

        return saveOrder(order)
    }

    fun sendOrderToQueue(order: OrderDataModel) {
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

    fun orderExists(id: Int): Boolean {
        return orderRepository.existsById(id)
    }

    fun getOrderById(id: Int): OrderDataModel? {
        return orderRepository.findById(id).map { it.toDataModel() }.orElse(null)
    }

    @Transactional
    fun updateOrderStatus(orderId: Int, newStatus: OrderState) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException("Order not found with id: $orderId") }
        validateStateTransition(order.state, newStatus)
        order.state = newStatus
        handleOrderStateChange(order, newStatus)
        orderRepository.save(order)
        if (newStatus == OrderState.OPEN) {
            sendOrderToQueue(order.toDataModel())
        }
    }

    private fun handleOrderStateChange(order: OrderEntity, newStatus: OrderState) {
        when (newStatus) {
            OrderState.FULLY_MATCHED -> order.remainingQuantity = order.quantity
            OrderState.CANCELLED, OrderState.EXPIRED -> {
                // Handle fund return logic here
            }

            else -> {} // No action needed for other states
        }
    }

    private fun validateStateTransition(currentState: OrderState, newState: OrderState) {
        val validTransitions = mapOf(
            OrderState.OPEN to setOf(
                OrderState.PARTIALLY_MATCHED,
                OrderState.FULLY_MATCHED,
                OrderState.CANCELLED,
                OrderState.EXPIRED
            ),
            OrderState.PARTIALLY_MATCHED to setOf(
                OrderState.FULLY_MATCHED,
                OrderState.CANCELLED,
                OrderState.EXPIRED
            ),
            OrderState.FULLY_MATCHED to emptySet(),
            OrderState.CANCELLED to emptySet(),
            OrderState.EXPIRED to emptySet()
        )
        if (newState !in (validTransitions[currentState] ?: emptySet())) {
            throw IllegalStateException("Invalid state transition from $currentState to $newState")
        }
    }

    @Transactional
    fun cancelOrder(orderId: Int) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException("Order not found with id: $orderId") }
        if (order.state == OrderState.OPEN || order.state == OrderState.PARTIALLY_MATCHED) {
            updateOrderStatus(orderId, OrderState.CANCELLED)
        } else {
            throw IllegalStateException("Cannot cancel order in state ${order.state}")
        }
    }

    @Transactional
    fun cancelAllOpenOrders(questionId: Int) {
        val openOrders = orderRepository.findByPulseIdAndState(questionId, OrderState.OPEN)
        openOrders.forEach { order ->
            order.state = OrderState.CANCELLED
            orderRepository.save(order)
            // Optionally, refund the user for the cancelled order
            // userService.refundOrder(order)
        }
    }
}
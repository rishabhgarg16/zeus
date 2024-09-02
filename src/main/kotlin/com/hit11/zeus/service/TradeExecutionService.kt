//package com.hit11.zeus.service
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.hit11.zeus.model.Order
//import com.hit11.zeus.model.Trade
//import com.hit11.zeus.repository.OrderRepository
//import com.hit11.zeus.repository.TradeRepository
//import org.springframework.stereotype.Service
//import software.amazon.awssdk.services.sqs.SqsClient
//import software.amazon.awssdk.services.sqs.model.SendMessageRequest
//import java.math.BigDecimal
//import javax.transaction.Transactional
//
//@Service
//class TradeExecutionService(
//    private val tradeRepository: TradeRepository,
//    private val orderRepository: OrderRepository,
//    private val userService: UserService,
//    private val sqsClient: SqsClient,
//    private val objectMapper: ObjectMapper
//) {
//    @Transactional
//    fun executeTrade(trade: Trade) {
//        // Save trade to database
//        tradeRepository.save(trade)
//
//        // Update user balances
//        updateUserBalances(trade)
//
//        // Publish trade event
//        publishTradeEvent(trade)
//    }
//
//    private fun updateUserBalances(trade: Trade) {
//        val buyOrder = getOrder(trade.buyerOrderId)
//        val sellOrder = getOrder(trade.sellerOrderId)
//
//        val tradeAmount = trade.price.multiply(BigDecimal(trade.quantity))
//        userService.updateUserWallet(buyOrder.userId, -tradeAmount.toDouble())
//        userService.updateUserWallet(sellOrder.userId, tradeAmount.toDouble())
//
//    }
//
//    private fun publishTradeEvent(trade: Trade) {
//        val eventJson = objectMapper.writeValueAsString(trade)
//        val sendRequest = SendMessageRequest.builder()
//            .queueUrl("https://sqs.{region}.amazonaws.com/{account-id}/trades-queue")
//            .messageBody(eventJson)
//            .build()
//        sqsClient.sendMessage(sendRequest)
//    }
//
//
//    private fun getOrder(orderId: Long): Order {
//        return orderRepository.findById(orderId)
//            .orElseThrow { IllegalStateException("Order not found: $orderId") }
//    }
//}
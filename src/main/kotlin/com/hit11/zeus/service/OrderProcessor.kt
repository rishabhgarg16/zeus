//package com.hit11.zeus.service
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.hit11.zeus.config.AwsProperties
//import com.hit11.zeus.model.Order
//import org.slf4j.LoggerFactory
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Component
//import software.amazon.awssdk.services.sqs.SqsClient
//import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
//import software.amazon.awssdk.services.sqs.model.Message
//import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
//
//@Component
//class OrderProcessor(
//    private val sqsClient: SqsClient,
//    private val orderOrchestrator: OrderOrchestrator,
//    private val objectMapper: ObjectMapper,
//    private val awsProperties: AwsProperties
//) {
//
//    private val logger = LoggerFactory.getLogger(this::class.java)
//
//    @Scheduled(fixedDelay = 1000) // Process messages every second
//    fun processOrders() {
//        val receiveRequest = ReceiveMessageRequest.builder()
//            .queueUrl(awsProperties.sqs.queueUrl)
//            .maxNumberOfMessages(10)
//            .build()
//
//        val messages = sqsClient.receiveMessage(receiveRequest).messages()
//
//        for (message in messages) {
//            try {
//                logger.info("Received message: ${message.body()}")
//                val order = objectMapper.readValue(message.body(), Order::class.java)
//                orderOrchestrator.processOrder(order)
//                deleteMessage(message)
//            } catch (e: Exception) {
//                logger.error("Error processing message: ${message.body()}", e)
//                // Handle error (e.g., log it, send to DLQ, etc.)
//            }
//        }
//    }
//
//    private fun deleteMessage(message: Message) {
//        val deleteRequest = DeleteMessageRequest.builder()
//            .queueUrl(awsProperties.sqs.queueUrl)
//            .receiptHandle(message.receiptHandle())
//            .build()
//        sqsClient.deleteMessage(deleteRequest)
//    }
//}
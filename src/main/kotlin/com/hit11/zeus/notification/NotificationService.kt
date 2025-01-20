package com.hit11.zeus.notification

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.Order
import com.hit11.zeus.model.Trade
import com.hit11.zeus.websocket.WebSocketHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val fcmService: FCMService,
    private val coroutineScope: CoroutineScope,
    private val webSocketHandler: WebSocketHandler
) {
    private val logger = Logger.getLogger(NotificationService::class.java)

    fun sendNotificationAsync(payload: NotificationPayload) {
        coroutineScope.launch {
            try {
                when (payload.deliveryType) {
                    DeliveryType.WEBSOCKET -> {
                        webSocketHandler.sendMessageToTopic("notifications_${payload.userId}", payload)
                    }

                    DeliveryType.FCM -> {
                        fcmService.sendNotification(payload)
                    }

                    DeliveryType.BOTH -> {
                        // Parallel execution for WebSocket and FCM
                        coroutineScope.launch {
                            webSocketHandler.sendMessageToTopic("notifications_${payload.userId}", payload)
                        }
                        coroutineScope.launch {
                            fcmService.sendNotification(payload)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to handle notification for user ${payload.userId}", e)
            }
        }
    }

    fun notifyTradeCreated(trade: Trade) {
        val notification = NotificationPayload(
            userId = trade.userId,
            type = NotificationType.TRADE_PLACED,
            title = "Trade Created",
            message = "New trade created for ${trade.quantity} units at ₹${trade.price}",
            metadata = mapOf(
                "tradeId" to trade.id.toString(),
                "pulseId" to trade.pulseId.toString(),
                "matchId" to trade.matchId.toString(),
                "side" to trade.orderSide.toString()
            ),
            deliveryType = DeliveryType.BOTH
        )
        sendNotificationAsync(notification)
    }

    fun notifyOrderCancelled(order: Order) {
        val notification = NotificationPayload(
            userId = order.userId,
            type = NotificationType.ORDER_CANCELLED,
            title = "Order Cancelled",
            message = "Your ${order.orderSide} order for ${order.quantity} units at ₹${order.price} has been cancelled",
            metadata = mapOf(
                "orderId" to order.id.toString(),
                "pulseId" to order.pulseId.toString(),
                "matchId" to order.matchId.toString()
            ),
            deliveryType = DeliveryType.BOTH
        )
        sendNotificationAsync(notification)
    }

}

enum class DeliveryType {
    WEBSOCKET,  // Only in-app notifications
    FCM,        // Only push notifications
    BOTH        // Both in-app and push
}
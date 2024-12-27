package com.hit11.zeus.notification

import com.hit11.zeus.websocket.WebSocketHandler
import com.hit11.zeus.exception.Logger
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

    fun handleNotification(payload: NotificationPayload) {
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
}

enum class DeliveryType {
    WEBSOCKET,  // Only in-app notifications
    FCM,        // Only push notifications
    BOTH        // Both in-app and push
}
package com.hit11.zeus.notification

import java.time.Instant
import java.util.*

data class NotificationPayload(
    val id: String = UUID.randomUUID().toString(),
    val userId: Int = 0,
    val type: NotificationType = NotificationType.ORDER_FAILED,
    val title: String = "Test Notification",
    val message: String = "Test Notification Message",
    val metadata: Map<String, Any> = emptyMap(),
    val createdAt: Instant = Instant.now(),
    val deliveryType: DeliveryType = DeliveryType.BOTH
)
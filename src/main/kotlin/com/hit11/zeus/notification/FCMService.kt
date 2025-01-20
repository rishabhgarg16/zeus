package com.hit11.zeus.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class FCMService(
    private val firebaseMessaging: FirebaseMessaging,
    private val userService: UserService
) {
    private val logger = Logger.getLogger(this::class.java)
    private val DEFAULT_FCM_TOKEN = "dVDASd-RRIGI1EtAanCYRb:APA91bEqM-ELWGn52knNPEZvRXTbQmXzbLVRJtt0EmU1koZBwpk6zze3JonMatoxV_oj_lf0RcMHWPcler_pjNUuHOyBY4oqlYerpPYXDeFAm83PxSOejWw"

    suspend fun sendNotification(payload: NotificationPayload) {
        try {
            val user = userService.getUserByUserId(payload.userId)
            val fcmToken = user?.fcmToken ?: DEFAULT_FCM_TOKEN

            // Convert metadata values to strings

            val metadata = mutableMapOf<String, String>()
            metadata["type"] = payload.type.toString() // Add notification type

            // Add other metadata
            payload.metadata.forEach { (key, value) ->
                metadata[key] = value.toString()
            }

            val message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                    com.google.firebase.messaging.Notification.builder()
                        .setTitle(payload.title)
                        .setBody(payload.message)
                        .build()
                )
                .putAllData(metadata)
                .build()

            withContext(Dispatchers.IO) {
                firebaseMessaging.send(message)
            }
        } catch (e: Exception) {
            logger.error("Failed to send FCM notification to user ${payload.userId}", e)
        }
    }
}
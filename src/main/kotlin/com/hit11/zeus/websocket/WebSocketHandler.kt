package com.hit11.zeus.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.notification.NotificationPayload
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

enum class MessageType {
    LIVE_SCORE,
    NOTIFICATION
}


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class WebSocketHandler(
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val logger = Logger.getLogger(WebSocketHandler::class.java)
    private val sessions = ConcurrentHashMap<String, WebSocketSession>()
    private val lastSessionMessage = ConcurrentHashMap<String, CharSequence>()
    private val matchSubscriptions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()
    private val userNotifications = ConcurrentHashMap<Int, WebSocketSession>()


    @PostConstruct
    fun init() {
        logger.info("WebSocketHandler initialized. Instance: ${this.hashCode()}")
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val sessionId = session.id
        sessions[sessionId] = session
        logger.info(
            "New WebSocket connection: $sessionId. Total sessions: ${sessions.size}. Handler instance: ${this.hashCode()}"
        )
        lastSessionMessage[session.id]?.let {
            session.sendMessage(TextMessage(it))
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        logger.info("WebSocketHandler Received message: $payload")

        try {
            val subscriptionMessage = objectMapper.readValue<WebSocketSubscriptionMessage>(message.payload)

            when (subscriptionMessage.action.uppercase()) {
                SubscriptionAction.SUBSCRIBE.name -> handleSubscribe(subscriptionMessage, session)
                SubscriptionAction.UNSUBSCRIBE.name -> handleUnsubscribe(subscriptionMessage, session)
                else -> logger.warn("Unknown action received: ${subscriptionMessage.action}")
            }
        } catch (e: Exception) {
            logger.error("WebSocketHandler Error processing message", e)
        }
    }

    private fun handleSubscribe(message: WebSocketSubscriptionMessage, session: WebSocketSession) {
        when (message.type) {
            SubscriptionType.MATCH.name -> {
                val matchId = message.metadata["matchId"] as String
                matchSubscriptions.getOrPut(matchId) { ConcurrentHashMap.newKeySet() }.add(session)
                logger.info("WebSocketHandler Session ${session.id} subscribed to match $matchId")
            }

            SubscriptionType.NOTIFICATION.name -> {
                val userId = (message.metadata["userId"] as Number).toInt()
                userNotifications[userId] = session
                logger.info("WebSocketHandler User $userId subscribed to notifications")
            }
        }
    }

    private fun handleUnsubscribe(message: WebSocketSubscriptionMessage, session: WebSocketSession) {
        when (message.type) {
            SubscriptionType.MATCH.name -> {
                val matchId = message.metadata["matchId"] as String
                matchSubscriptions[matchId]?.remove(session)
                logger.info("WebSocketHandler Session ${session.id} unsubscribed from match $matchId")
            }

            SubscriptionType.NOTIFICATION.name -> {
                val userId = (message.metadata["userId"] as Number).toInt()
                userNotifications.remove(userId)
                logger.info("WebSocketHandler User $userId unsubscribed from notifications")
            }
        }
    }

    fun sendMessageToTopic(topic: String, message: Any) {
        try {
            when (message) {
                is Hit11Scorecard -> {
                    val matchId = topic.removePrefix("match")
                    matchSubscriptions[matchId]?.forEach { session ->
                        try {
                            val webSocketMessage = createLiveScoreMessage(topic, message)
                            session.sendMessage(TextMessage(webSocketMessage))
                            logger.info(
                                "WebSocketHandler Sent live score update for match $matchId to session ${session.id}"
                            )
                        } catch (e: Exception) {
                            logger.error("Failed to send live score update to session ${session.id}", e)
                            // Optionally remove failed session
                            matchSubscriptions[matchId]?.remove(session)
                        }
                    }
                }

                is NotificationPayload -> {
                    userNotifications[message.userId]?.let { session ->
                        try {
                            val webSocketMessage = createNotificationMessage(topic, message)
                            session.sendMessage(TextMessage(webSocketMessage))
                            logger.info("WebSocketHandler Sent notification to user ${message.userId}")
                        } catch (e: Exception) {
                            logger.error("Failed to send notification to user ${message.userId}", e)
                            // Optionally remove failed session
                            userNotifications.remove(message.userId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Catch any other unexpected errors
            logger.error("Unexpected error in message sending", e)
        }
    }

    fun broadcastToAllSessions(message: String) {
        sessions.values.forEach { session -> // TODO can do async
            try {
                session.sendMessage(TextMessage(message))
            } catch (e: Exception) {
                logger.error("WebSocketHandler Failed to send price update to session ${session.id}", e)
            }
        }
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus
    ) {
        sessions.remove(session.id)
        logger.info("WebSocket connection closed: ${session.id}")
    }

    private fun createLiveScoreMessage(topic: String, scorecard: Hit11Scorecard): String {
        return objectMapper.writeValueAsString(
            mapOf(
                "type" to MessageType.LIVE_SCORE,
                "topic" to topic,
                "matchId" to topic.removePrefix("match"),
                "data" to scorecard
            )
        )
    }

    private fun createNotificationMessage(topic: String, notification: NotificationPayload): String {
        val clientNotification = mapOf(
            "id" to notification.id,
            "userId" to notification.userId,
            "type" to notification.type,
            "title" to notification.title,
            "message" to notification.message,
            "timestamp" to notification.createdAt.epochSecond,  // Convert Instant to epoch seconds
            "isRead" to false,
            "metadata" to notification.metadata
        )
        return objectMapper.writeValueAsString(
            mapOf(
                "type" to MessageType.NOTIFICATION,
                "topic" to topic,
                "data" to clientNotification
            )
        )
    }
}
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
//    private val sessionToUser = ConcurrentHashMap<String, Int>() // Map session to userId
    private val lastSessionMessage = ConcurrentHashMap<String, CharSequence>()
    private val matchSubscriptions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()
    private val userNotifications = ConcurrentHashMap<Int, WebSocketSession>()


    @PostConstruct
    fun init() {
        println("WebSocketHandler initialized. Instance: ${this.hashCode()}")
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val sessionId = session.id
        sessions[sessionId] = session
        println(
            "New WebSocket connection: $sessionId. Total sessions: ${sessions.size}. Handler instance: ${this.hashCode()}"
        )
        lastSessionMessage[session.id]?.let {
            session.sendMessage(TextMessage(it))
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val payload = message.payload
        println("Received message: $payload")

        try {
            val subscriptionMessage = objectMapper.readValue<WebSocketSubscriptionMessage>(message.payload)

            when (subscriptionMessage.action.uppercase()) {
                SubscriptionAction.SUBSCRIBE.name -> handleSubscribe(subscriptionMessage, session)
                SubscriptionAction.UNSUBSCRIBE.name -> handleUnsubscribe(subscriptionMessage, session)
                else -> logger.warn("Unknown action received: ${subscriptionMessage.action}")
            }
        } catch (e: Exception) {
            logger.error("Error processing message", e)
        }
    }

    private fun handleSubscribe(message: WebSocketSubscriptionMessage, session: WebSocketSession) {
        when (message.type) {
            SubscriptionType.MATCH.name -> {
                val matchId = message.metadata["matchId"] as String
                matchSubscriptions.getOrPut(matchId) { ConcurrentHashMap.newKeySet() }.add(session)
                logger.info("Session ${session.id} subscribed to match $matchId")
            }
            SubscriptionType.NOTIFICATION.name -> {
                val userId = (message.metadata["userId"] as Number).toInt()
                userNotifications[userId] = session
                logger.info("User $userId subscribed to notifications")
            }
        }
    }

    private fun handleUnsubscribe(message: WebSocketSubscriptionMessage, session: WebSocketSession) {
        when (message.type) {
            SubscriptionType.MATCH.name -> {
                val matchId = message.metadata["matchId"] as String
                matchSubscriptions[matchId]?.remove(session)
                logger.info("Session ${session.id} unsubscribed from match $matchId")
            }
            SubscriptionType.NOTIFICATION.name -> {
                val userId = (message.metadata["userId"] as Number).toInt()
                userNotifications.remove(userId)
                logger.info("User $userId unsubscribed from notifications")
            }
        }
    }

    fun sendMessageToTopic(topic: String, message: Any) {
        try {
            when (message) {
                is Hit11Scorecard -> {
                    val matchId = topic.removePrefix("match")
                    matchSubscriptions[matchId]?.forEach { session ->
                        val webSocketMessage = createLiveScoreMessage(topic, message)
                        session.sendMessage(TextMessage(webSocketMessage))
                        logger.info("Sent live score update for match $matchId")
                    }
                }
                is NotificationPayload -> {
                    userNotifications[message.userId]?.let { session ->
                        val webSocketMessage = createNotificationMessage(topic, message)
                        session.sendMessage(TextMessage(webSocketMessage))
                        logger.info("Sent notification to user ${message.userId}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error sending message", e)
        }
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus
    ) {
        sessions.remove(session.id)
        println("WebSocket connection closed: ${session.id}")
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

//        sessions.values.forEach { session ->
//            val subscribedTopicsBySession = session.attributes["subscribedTopics"] as? Set<String>
//
//            // For notifications, check if the user is the target recipient
//            if (message is NotificationPayload) {
//                val sessionUserId = sessionToUser[session.id]
//                if (sessionUserId == message.userId) {
//                    val jsonMessage = objectMapper.writeValueAsString(webSocketMessage)
//                    session.sendMessage(TextMessage(jsonMessage))
//                    println("Sent notification to user $sessionUserId")
//                }
//            } else if (subscribedTopicsBySession?.contains(topic) == true) {
//                println("Session ${session.id} subscribed topics: $subscribedTopicsBySession")
//                val jsonMessage = JsonObject().apply {
//                    addProperty(
//                        "topic",
//                        topic
//                    )
//                    addProperty("matchId", topic.removePrefix("match"))
//                    addProperty("liveScore", Gson().toJson(message))
//                }.toString()
//                lastSessionMessage[session.id] = jsonMessage
//                if (subscribedTopicsBySession?.contains(topic) == true) {
//                    session.sendMessage(TextMessage(jsonMessage))
//                    println("Sent message to session ${session.id} for topic $topic")
//                }
//            }
//        }
//    }


//    private fun handleSubscribe(
//        topic: String?,
//        session: WebSocketSession
//    ) {
//        if (topic == null) return
//        val subscribedTopics =
//            session.attributes.getOrPut("subscribedTopics") { mutableSetOf<String>() } as MutableSet<String>
//        subscribedTopics.add(topic)
//        println("Session ${session.id} subscribed to topic: $topic")
//    }
//
//    private fun handleUnsubscribe(
//        topic: String?,
//        session: WebSocketSession
//    ) {
//        if (topic == null) return
//        val subscribedTopics = session.attributes["subscribedTopics"] as? MutableSet<String>
//        subscribedTopics?.remove(topic)
//        println("Session ${session.id} unsubscribed from topic: $topic")
//    }
}
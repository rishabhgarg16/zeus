package com.hit11.zeus.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.JsonObject
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class MyWebSocketHandler(
    private val objectMapper: ObjectMapper
) : TextWebSocketHandler() {

    private val sessions = ConcurrentHashMap<String, WebSocketSession>()

    @PostConstruct
    fun init() {
        println("MyWebSocketHandler initialized. Instance: ${this.hashCode()}")
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val sessionId = session.id
        sessions[sessionId] = session
        println("New WebSocket connection: $sessionId. Total sessions: ${sessions.size}. Handler instance: ${this.hashCode()}")
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage
    ) {
        val payload = message.payload
        println("Received message: $payload")

        // Parse the message and handle subscriptions/unsubscriptions
        val jsonMessage = objectMapper.readValue<Map<String, String>>(message.payload)

        when (jsonMessage["action"]) {
            "subscribe" -> handleSubscribe(jsonMessage["topic"], session)
            "unsubscribe" -> handleUnsubscribe(jsonMessage["topic"], session)
            else -> println("Unknown action")
        }
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus
    ) {
        sessions.remove(session.id)
        println("WebSocket connection closed: ${session.id}")
    }

    fun sendMessageToTopic(
        topic: String,
        message: String
    ) {
        println("Attempting to send message. Total sessions: ${sessions.size}. Handler instance: ${this.hashCode()}")
        sessions.values.forEach { session ->
            val subscribedTopicsBySession = session.attributes["subscribedTopics"] as? Set<String>
            println("Session ${session.id} subscribed topics: $subscribedTopicsBySession")
            if (subscribedTopicsBySession?.contains(topic) == true) {
                val jsonMessage = JsonObject().apply {
                    addProperty(
                        "topic",
                        topic
                    )
                    addProperty("matchId", topic.removePrefix("match"))
                    addProperty("liveScore", message)
                }.toString()
                session.sendMessage(TextMessage(jsonMessage))
                println("Sent message to session ${session.id} for topic $topic")
            }
        }
    }

    private fun handleSubscribe(
        topic: String?,
        session: WebSocketSession
    ) {
        if (topic == null) return
        val subscribedTopics =
            session.attributes.getOrPut("subscribedTopics") { mutableSetOf<String>() } as MutableSet<String>
        subscribedTopics.add(topic)
        println("Session ${session.id} subscribed to topic: $topic")
    }

    private fun handleUnsubscribe(
        topic: String?,
        session: WebSocketSession
    ) {
        if (topic == null) return
        val subscribedTopics = session.attributes["subscribedTopics"] as? MutableSet<String>
        subscribedTopics?.remove(topic)
        println("Session ${session.id} unsubscribed from topic: $topic")
    }
}
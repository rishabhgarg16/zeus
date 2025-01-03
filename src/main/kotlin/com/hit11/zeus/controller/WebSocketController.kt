package com.hit11.zeus.controller

import com.hit11.zeus.websocket.WebSocketHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/websocket")
class WebSocketController @Autowired constructor(
    private val webSocketHandler: WebSocketHandler
) {

    @PostMapping("/broadcast")
    fun broadcastMessage(@RequestBody message: BroadcastRequest): ResponseEntity<String> {
        return try {
            webSocketHandler.broadcastToAllSessions(message.content)
            ResponseEntity.ok("Message broadcasted to all sessions")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Failed to broadcast message: ${e.message}")
        }
    }

    @PostMapping("/send-to-topic")
    fun sendMessageToTopic(@RequestBody message: TopicMessageRequest): ResponseEntity<String> {
        return try {
            webSocketHandler.sendMessageToTopic(message.topic, message.content)
            ResponseEntity.ok("Message sent to topic: ${message.topic}")
        } catch (e: Exception) {
            ResponseEntity.badRequest().body("Failed to send message to topic: ${e.message}")
        }
    }
}

data class BroadcastRequest(
    val content: String = ""
)

data class TopicMessageRequest(
    val topic: String = "",
    val content: Any = ""
)

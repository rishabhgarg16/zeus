//package com.hit11.zeus.controller
//
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestBody
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/api/ws")
//class WebSocketController(private val webSocketHandler: MyWebSocketHandler) {
//
//    @PostMapping("/liveScore")
//    fun sendMatchData(@RequestBody request: LiveMatchRequest): ResponseEntity<String> {
//        try {
//            val score = "Team A: ${request.score} (10.0 ov)"
//            val topic = "match${request.matchId}"
//            println("Sending message to topic: $topic, score: $score")
//            webSocketHandler.sendMessageToTopic(topic, score)
//            return ResponseEntity.ok("Message sent to WebSocket")
//        } catch (e: Exception) {
//            println("${e.message}")
//            return ResponseEntity.badRequest().body("Error sending message to WebSocket")
//        }
//    }
//}
//
//data class LiveMatchRequest(
//    val matchId: Int = -1,
//    val score: String = ""
//)
package com.hit11.zeus.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.service.EventService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class LiveMatchRequest(
    val matchId: Int = -1, val score: String = ""
)

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService,
    private val webSocketHandler: MyWebSocketHandler
) {


    private val logger = Logger.getLogger(EventController::class.java)
    private val objectMapper = jacksonObjectMapper()

    @PostMapping("/liveScore")
    fun sendMatchData(
        @RequestBody request: LiveMatchRequest
    ): ResponseEntity<String> {
        try {
            val score = "Team A: ${request.score} (10.0 ov)"
            val topic = "match${request.matchId}"
            println("Sending message to topic: $topic, score: $score")
            webSocketHandler.sendMessageToTopic(topic, score)
            return ResponseEntity.ok("Message sent to WebSocket")
        } catch (e: Exception) {
            println("${e.message}")
            return ResponseEntity.badRequest().body("Error sending message to WebSocket")
        }
    }

    @PostMapping("/scorecard")
    fun sendMatchData(
        @RequestBody request: Hit11Scorecard
    ): ResponseEntity<String> {
        try {
            val score = "Team A: 11 (10.0 ov)"
            val topic = "match${request.matchId}"
            println("Sending message to topic: $topic, score: $score")
            eventService.processBallEvent(request)
            webSocketHandler.sendMessageToTopic(topic, score)
            return ResponseEntity.ok("Message sent to WebSocket")
        } catch (e: Exception) {
            println("${e.message}")
            return ResponseEntity.badRequest().body("Error sending message to WebSocket")
        }
    }



}
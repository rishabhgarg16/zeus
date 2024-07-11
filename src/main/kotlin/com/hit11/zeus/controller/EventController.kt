package com.hit11.zeus.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.LiveMatchData
import com.hit11.zeus.livedata.LiveMatchResponse
import com.hit11.zeus.model.BallEvent
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.model.response.UpdateQuestionsResponse
import com.hit11.zeus.service.EventService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.threeten.bp.Instant
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService,
//    private val messagingTemplate: SimpMessagingTemplate  // Inject SimpMessagingTemplate for WebSocke
) {

    private val restTemplate = RestTemplate()

    //    private val objectMapper = jacksonObjectMapper()
//    private val logger = Logger.getLogger(EventController::class.java)
    private val sessions = ConcurrentHashMap.newKeySet<String>()

    private val logger = Logger.getLogger(EventController::class.java)
    private val objectMapper = jacksonObjectMapper()
    private val topicSubscriptions = ConcurrentHashMap<String, MutableSet<String>>()

    @MessageMapping("/ws")
    fun handleWebSocketMessage(
        @Payload message: String,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        // app/ws
        val sessionId = headerAccessor.sessionId ?: return
        val messageMap = objectMapper.readValue<Map<String, String>>(message)

        when (messageMap["action"]) {
            "subscribe" -> handleSubscribe(
                messageMap["topic"],
                sessionId
            )

            "unsubscribe" -> handleUnsubscribe(
                messageMap["topic"],
                sessionId
            )

            else -> logger.warn("Unknown action received: ${messageMap["action"]}")
        }
    }

    private fun handleSubscribe(
        topic: String?,
        sessionId: String
    ) {
        if (topic == null) return
        topicSubscriptions.computeIfAbsent(topic) { ConcurrentHashMap.newKeySet() }.add(sessionId)
        logger.info("Client $sessionId subscribed to topic $topic")

        if (topicSubscriptions[topic]?.size == 1) {
            startSendingMessages(topic)
        }
    }

    private fun handleUnsubscribe(
        topic: String?,
        sessionId: String
    ) {
        if (topic == null) return
        topicSubscriptions[topic]?.remove(sessionId)
        logger.info("Client $sessionId unsubscribed from topic $topic")

        if (topicSubscriptions[topic].isNullOrEmpty()) {
            topicSubscriptions.remove(topic)
        }
    }

    private fun startSendingMessages(topic: String) {
        GlobalScope.launch {
            while (topicSubscriptions.containsKey(topic)) {
                val message = createMessageForTopic(topic)
                sendMessageToTopic(
                    topic,
                    message
                )
                delay(1000) // Send a message every second
            }
        }
    }

    private fun createMessageForTopic(topic: String): String {
        val messageContent = when {
            topic.startsWith("match") -> {
                val matchId = topic.removePrefix("match")
                val score = "Team A: 100/2 (10.0 ov)"
                """{"topic": "$topic", "message": "$score"}"""
            }

            else -> """{"topic": "$topic", "message": "Update for $topic: ${System.currentTimeMillis()}"}"""
        }
        return messageContent
    }

    private fun sendMessageToTopic(
        topic: String,
        message: String
    ) {
//        messagingTemplate.convertAndSend(
//            "/topic/$topic",
//            message
//        )
    }


    private fun broadcastMessage(message: String) {
        // Broadcast the message to all sessions
//        sessions.forEach { session ->
//            messagingTemplate.convertAndSend(
//                "/topic/event2",
//                message
//            )
//        }
    }

    //    @MessageMapping("/live/ws")
    @GetMapping("/livescorews")
    fun sendLiveScore(message: String) {
        // Add the new session to the set of sessions
//        sessions.add(message) // Assuming 'message' contains session identifier, adapt if necessary

//        GlobalScope.launch {
//            repeat(100000) { i ->  // Changed to 50 times for practicality
//                val currentTime = Instant.now()
//                println("Iteration $i: Received: $message at $currentTime")
//                myWebSocketHandler.sendMessageToTopic("match{matchId}", "Team A: 100/2 (10.0 ov))")
////                broadcastMessage("Received WebSocket message: $message at $currentTime")
//                delay(1000) // Delay for 1 second between iterations
//            }
//        }
    }


    @MessageMapping("/ball-event")
    @SendTo("/topic/event")
    fun receiveBallEvent(@RequestBody ballEvent: BallEvent): ResponseEntity<ApiResponse<UpdateQuestionsResponse>> {
//        val updateResponse = eventService.processBallEvent(ballEvent);
        val updateResponse = UpdateQuestionsResponse()
        sendEventUpdate(ballEvent)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Event processed successfully",
                data = updateResponse
            )
        )
    }

    //    @Scheduled(fixedRate = 60000)  // Fetch data every 60 seconds
    fun fetchLiveData() {
        val url =
                "https://demo.entitysport.com/wp-admin/admin-ajax.php?action=wpec_api_request&path=matches%2F73861%2Flive"
        val response = restTemplate.getForObject(
            url,
            String::class.java
        )

        if (response != null) {
            try {
                val liveMatchData = parseLiveMatchResponse(response)
                val filePath = "src/main/resources/demo_entity_live_score_response.json"
                val jsonResponse = readFileAsString(filePath)
                val matchResponse = parseLiveMatchResponse(jsonResponse)
                val ballEvents = transformToInternalModels(matchResponse)
                ballEvents.firstOrNull()?.let {
                    sendEventUpdate(it)
                }
            } catch (e: Exception) {
                logger.error(
                    "Error parsing or processing live match data",
                    e
                )
            }
        }
    }

    // Function to send ball event update to WebSocket clients
    private fun sendEventUpdate(ballEvent: BallEvent) {
//        val updateResponse = eventService.processBallEvent(ballEvent)
//        messagingTemplate.convertAndSend(
//            "/topic/event",
//            ballEvent
//        )
    }


    private fun parseLiveMatchResponse(jsonData: String): LiveMatchData {
        val liveMatchResponse: LiveMatchResponse = objectMapper.readValue(jsonData)
        return liveMatchResponse.response
    }

    private fun transformToInternalModels(liveMatchData: LiveMatchData): List<BallEvent> {
        val ballEvents = mutableListOf<BallEvent>()

        liveMatchData.commentaries.forEach { commentary ->
            val ballEvent = BallEvent(
                inningId = liveMatchData.liveInningNumber,
                event = commentary.event,
                teamName = liveMatchData.teamBatting,
                matchId = liveMatchData.mid,
                batsmanId = commentary.batsmanId.toInt(),
                bowlerId = commentary.bowlerId.toInt(),
                batsmanRuns = commentary.run,
                extraRuns = 0,  // Calculate based on other fields if needed
                overNumber = commentary.over.toInt(),
                ballNumber = commentary.ball?.toInt(),
                runsScored = commentary.run,
                wicketType = commentary.how_out,
                fielderId = null,  // Determine based on how_out if needed
                wicketkeeperCatch = false,  // Determine based on how_out if needed
                isWicket = commentary.event == "wicket",
                isWide = commentary.wideball,
                isNoBall = commentary.noball,
                isBye = false,  // Calculate based on other fields if needed
                isLegBye = false,  // Calculate based on other fields if needed
                isPenalty = false  // Calculate based on other fields if needed
            )
            ballEvents.add(ballEvent)
        }

        return ballEvents
    }


    @GetMapping("/test")
    fun testParser(): ApiResponse<LiveMatchData> {
        val filePath = "src/main/resources/demo_entity_live_score_response.json"
        val jsonResponse = readFileAsString(filePath)
        val matchResponse = parseLiveMatchResponse(jsonResponse)
        return ApiResponse(
            data = matchResponse,
            message = "Success",
            status = 200,
            internalCode = null
        )
    }

    // Function to read file content
    fun readFileAsString(filePath: String): String {
        val path = Paths.get(filePath)
        return String(
            Files.readAllBytes(path),
            StandardCharsets.UTF_8
        )
    }
}

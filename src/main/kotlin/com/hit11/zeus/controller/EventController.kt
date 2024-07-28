package com.hit11.zeus.controller

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.service.EventService
import com.hit11.zeus.service.QuestionService
import com.hit11.zeus.service.UpdateQuestionsResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class LiveMatchRequest(
    val matchId: Int = -1, val score: String = ""
)

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService,
    private val webSocketHandler: MyWebSocketHandler,
    private val questionService: QuestionService
) {
    private val logger = Logger.getLogger(EventController::class.java)

//    @PostMapping("/scorecard")
//    fun sendMatchData(
//        @RequestBody request: Hit11Scorecard
//    ): ResponseEntity<String> {
//        try {
//            val score = "Team A: 11 (10.0 ov)"
//            val topic = "match${request.matchId}"
//            println("Sending message to topic: $topic, score: $score")
//            eventService.processBallEvent(request)
//            webSocketHandler.sendMessageToTopic(topic, score)
//            return ResponseEntity.ok("Message sent to WebSocket")
//        } catch (e: Exception) {
//            println("${e.message}")
//            return ResponseEntity.badRequest().body("Error sending message to WebSocket")
//        }
//    }

    @PostMapping("/scorecardV2")
    fun newSendMatchData(
        @RequestBody scoreCard: Hit11Scorecard
    ): ResponseEntity<UpdateQuestionsResponse> {
        // Call QuestionService to update questions based on the ball event
        val currentInnings = scoreCard.innings.find{ it.isCurrentInnings == true}
        val latestBallEvent = currentInnings?.ballByBallEvents?.sortedByDescending { it.ballNumber }
        logger.info("[QuestionUpdate] processing scorecard for match ${scoreCard.matchId} and ball number $latestBallEvent")
        val updatedQuestionsResponse =
            questionService.updateQuestions(scoreCard)
        val topic = "match${scoreCard.matchId}"
        logger.info("[WSLiveScore] sending data to ws for match ${scoreCard.matchId} and ball number $latestBallEvent")
        webSocketHandler.sendMessageToTopic(topic, scoreCard)
        logger.info("[WSLiveScore] data sent to ws for match ${scoreCard.matchId} and ball number $latestBallEvent")
        return ResponseEntity.ok(updatedQuestionsResponse)
    }
}
package com.hit11.zeus.controller

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.service.EventService
import com.hit11.zeus.service.QuestionError
import com.hit11.zeus.service.QuestionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class LiveMatchRequest(
    val matchId: Int = -1, val score: String = ""
)

data class BallEventProcessResponse (
    val updatedQuestions: List<QuestionDataModel>,
    val notUpdatedQuestions: List<QuestionDataModel>,
    val newQuestions: List<QuestionDataModel>?,
    val errors: List<QuestionError>
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
    ): ResponseEntity<BallEventProcessResponse> {
        // Call QuestionService to update questions based on the ball event
        val currentInnings =
            scoreCard.innings.find { it.isCurrentInnings == true }
        val latestBallEvent =
            currentInnings?.ballByBallEvents?.sortedByDescending { it.ballNumber }
                ?.firstOrNull()
        logger.info(
            "[QuestionUpdate] processing scorecard for match ${scoreCard.matchId} and ball number $latestBallEvent"
        )

        val updatedQuestionsResponse =
            questionService.processNewScorecard(scoreCard)

        logger.info(
            "[QuestionUpdate] processed scorecard for match ${scoreCard.matchId} and ball number ${latestBallEvent?.ballNumber}"
        )

        val topic = "match${scoreCard.matchId}"
        logger.info("[WSLiveScore] sending data to ws for match ${scoreCard.matchId} and ball number $latestBallEvent")
        webSocketHandler.sendMessageToTopic(
            topic,
            scoreCard
        )
        logger.info("[WSLiveScore] data sent to ws for match ${scoreCard.matchId} and ball number $latestBallEvent")
        return ResponseEntity.ok(updatedQuestionsResponse)
    }
}
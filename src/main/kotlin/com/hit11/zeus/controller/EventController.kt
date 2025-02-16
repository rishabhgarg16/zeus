package com.hit11.zeus.controller

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.Question
import com.hit11.zeus.service.EventService
import com.hit11.zeus.service.QuestionError
import com.hit11.zeus.service.QuestionService
import com.hit11.zeus.websocket.WebSocketHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class BallEventProcessResponse(
    val updatedQuestions: List<Question>,
    val notUpdatedQuestions: List<Question>,
    val newQuestions: List<Question>?,
    val errors: List<QuestionError>
)

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService,
    private val webSocketHandler: WebSocketHandler,
    private val questionService: QuestionService
) {
    private val logger = Logger.getLogger(EventController::class.java)

    @PostMapping("/scorecardV2")
    fun newSendMatchData(
        @RequestBody scoreCard: Hit11Scorecard
    ): ResponseEntity<BallEventProcessResponse> {
        // Call QuestionService to update questions based on the ball event
        try {
            // Log details about the current innings and latest ball event.
            val currentInnings = scoreCard.innings.find { it.isCurrentInnings == true }
            val latestBallEvent = currentInnings?.ballByBallEvents?.sortedByDescending { it.ballNumber }?.firstOrNull()
            logger.info("[QuestionUpdate] processing scorecard for match ${scoreCard.matchId} and ball number $latestBallEvent")

            // Process the scorecard, which includes both question updates and generation.
            val updatedQuestionsResponse = questionService.processNewScorecard(scoreCard)

            logger.info("[QuestionUpdate] processed scorecard for match ${scoreCard.matchId} and ball number ${latestBallEvent?.ballNumber}")

            // Send the scorecard update via web socket.
            logger.info("[WSLiveScore] sending data to ws for match ${scoreCard.matchId} and ball number $latestBallEvent")
            webSocketHandler.sendMessageToTopic(
                topic = "match${scoreCard.matchId}",
                message = scoreCard
            )
            logger.info("[WSLiveScore] data sent to ws for match ${scoreCard.matchId} and ball number $latestBallEvent")

            // Return the combined response, including any errors encountered during processing.
            return ResponseEntity.ok(updatedQuestionsResponse)
        } catch (e: Exception) {
            logger.error("Error processing scorecard for match ${scoreCard.matchId}", e)
            return ResponseEntity.badRequest().body(
                BallEventProcessResponse(
                    updatedQuestions = emptyList(),
                    notUpdatedQuestions = emptyList(),
                    newQuestions = emptyList(),
                    errors = listOf(QuestionError(-1, "Error processing scorecard: ${e.message}"))
                )
            )
        }
    }
}
package com.hit11.zeus.service

import com.hit11.zeus.controller.BallEventProcessResponse
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.*
import com.hit11.zeus.model.request.QuestionAnswerUpdateRequest
import com.hit11.zeus.model.response.QuestionAnswerUpdateResponse
import com.hit11.zeus.question.QuestionGenerator
import com.hit11.zeus.question.ResolutionStrategy
import com.hit11.zeus.repository.QuestionRepository
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import javax.transaction.Transactional

@Service
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val questionGenerators: List<QuestionGenerator<*>>,
    private val resolutionStrategies: Map<QuestionType, ResolutionStrategy>,
    private val payoutService: PayoutService,
    private val orderExecutionService: OrderExecutionService
) {
    private var previousState: MatchState? = null
    private var lastProcessedBallNumber: Int = 0

    fun getAllActivePulses(): List<Question>? {
        return questionRepository.findAllByStatusAndPulseEndDateAfter(
            QuestionStatus.LIVE, ZonedDateTime.now().toInstant()
        )
    }

    private val logger = Logger.getLogger(QuestionService::class.java)
    fun getAllActiveQuestionsByMatch(matchIdList: List<Int>): List<Question>? {
        return questionRepository.findByMatchIdInAndStatusAndPulseEndDateAfter(
            matchIdList, QuestionStatus.LIVE, ZonedDateTime.now().toInstant()
        )
    }

    @Transactional
    fun updateQuestionAnswer(
        answerUpdateRequest: QuestionAnswerUpdateRequest
    ): QuestionAnswerUpdateResponse {
        val response = QuestionAnswerUpdateResponse()
        try {
            val pulse = questionRepository.getPulseById(answerUpdateRequest.pulseId)
            val pulseResult = answerUpdateRequest.pulseResult
            val updatedQuestion = updateQuestion(pulse, pulseResult)
            payoutService.processPayouts(updatedQuestion, pulseResult)
        } catch (e: Exception) {
            logger.error("Error updating question answer", e)
            throw e
        }
        return response
    }

    fun processNewScorecard(scorecard: Hit11Scorecard): BallEventProcessResponse {
        val currentState = MatchState(scorecard)
        val currentInnings = scorecard.innings.find { it.isCurrentInnings }
        val latestBallNumber = currentInnings?.ballByBallEvents?.maxByOrNull { it.ballNumber }?.ballNumber ?: 0

        if (scorecard.state == CricbuzzMatchPlayingState.IN_PROGRESS && latestBallNumber < lastProcessedBallNumber) {
            return BallEventProcessResponse(
                emptyList(), emptyList(), emptyList(),
                listOf(QuestionError(-1, "Ball sequence not incremental"))
            )
        }

        val updatedQuestionsResponse = updateQuestions(currentState)
        val generatedResponse = generateQuestions(currentState)

        previousState = currentState
        lastProcessedBallNumber = latestBallNumber

        val allErrors = updatedQuestionsResponse.errors + generatedResponse.errors

        return BallEventProcessResponse(
            updatedQuestions = updatedQuestionsResponse.updatedQuestions,
            notUpdatedQuestions = updatedQuestionsResponse.notUpdatedQuestions,
            newQuestions = generatedResponse.newQuestions,
            errors = allErrors
        )
    }

    @Transactional
    fun activateQuestionsForLiveMatches(): Int {
        try {
            return questionRepository.activateQuestionsForLiveMatches(
                newStatus = QuestionStatus.LIVE,
                matchStatus = "In Progress",
                currentStatus = QuestionStatus.SYSTEM_GENERATED
            )
        } catch (e: Exception) {
            logger.error("Error activating questions for live matches", e)
            throw RuntimeException("Failed to activate questions: ${e.message}")
        }
    }

    @Transactional
    private fun updateQuestions(
        matchState: MatchState
    ): UpdateQuestionsResponse {
        val matchId = matchState.liveScorecard.matchId
        val questions = questionRepository.findByMatchIdAndStatusIn(
            matchId,
            listOf(QuestionStatus.LIVE, QuestionStatus.DISABLED)
        ) ?: listOf()

        val updatedQuestions = mutableListOf<Question>()
        val notUpdatedQuestions = mutableListOf<Question>()
        val errors = mutableListOf<QuestionError>()

        for (question in questions) {
            try {
                val resolutionStrategy = resolutionStrategies[question.questionType]
                if (resolutionStrategy?.canResolve(question, matchState) == true) {
                    val resolution = resolutionStrategy.resolve(question, matchState)
                    if (resolution.isResolved) {
                        val updatedQuestion = updateQuestion(question, resolution.result)
                        if (resolution.result != PulseResult.UNDECIDED) {
                            payoutService.processPayouts(updatedQuestion, resolution.result)
                        }
                        updatedQuestions.add(updatedQuestion)
                    } else {
                        notUpdatedQuestions.add(question)
                    }
                } else {
                    notUpdatedQuestions.add(question)
                }
            } catch (e: QuestionValidationException) {
                logger.error("Validation error for question ${question.id}", e)
                errors.add(QuestionError(question.id, e.message ?: "Validation error"))
                notUpdatedQuestions.add(question)
            } catch (e: Exception) {
                logger.error("Error processing question ${question.id}", e)
                errors.add(QuestionError(question.id, "Error processing question: ${e.message}"))
                notUpdatedQuestions.add(question)
            }
        }

        return UpdateQuestionsResponse(
            updatedQuestions, notUpdatedQuestions, errors
        )
    }

    @Transactional
    private fun updateQuestion(
        question: Question,
        result: PulseResult
    ): Question {
        try {
            if (question.status == QuestionStatus.RESOLVED) {
                throw QuestionValidationException("Pulse ${question.id} is already resolved.")
            }
            question.pulseResult = result
            question.status = QuestionStatus.RESOLVED
            questionRepository.save(question)
            return question
        } catch (e: Exception) {
            logger.error("Error updating question ${question.id}", e)
            throw e
        }
    }

    private fun generateQuestions(currentState: MatchState): GeneratedQuestionsResponse {
        val generatedQuestions = mutableListOf<Question>()
        val generationErrors = mutableListOf<QuestionError>()

        questionGenerators.forEach { generator ->
            try {
                val questionsFromGenerator = generator.generateQuestions(currentState, previousState)
                generatedQuestions.addAll(questionsFromGenerator)
            } catch (e: Exception) {
                val errorMsg = "Error generating questions with ${generator::class.simpleName}: ${e.message}"
                logger.error(errorMsg, e)
                generationErrors.add(QuestionError(questionId = -1,  errorMessage = errorMsg))
            }
        }
        // Save generated questions;
        questionRepository.saveAll(generatedQuestions)

        return GeneratedQuestionsResponse(
            newQuestions = generatedQuestions,
            errors = generationErrors
        )
    }

    fun getQuestionById(questionId: Int): Question? {
        return questionRepository.findById(questionId).orElse(null)
    }
}

data class QuestionError(
    val questionId: Int, val errorMessage: String
)

data class UpdateQuestionsResponse(
    val updatedQuestions: List<Question>,
    val notUpdatedQuestions: List<Question>,
    val errors: List<QuestionError>
)

data class GeneratedQuestionsResponse(
    val newQuestions: List<Question>,
    val errors: List<QuestionError>
)
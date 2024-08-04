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

@Service
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val questionGenerators: List<QuestionGenerator<*>>,
    private val resolutionStrategies: Map<QuestionType, ResolutionStrategy>,
    private val payoutService: PayoutService
) {
    private var previousState: MatchState? = null
    private var lastProcessedBallNumber: Int = 0

    private val logger = Logger.getLogger(QuestionService::class.java)
    fun getAllActiveQuestions(matchIdList: List<Int>): List<QuestionDataModel>? {
        return questionRepository.findByMatchIdInAndStatus(matchIdList, QuestionStatus.LIVE)
            ?.map { it.mapToQuestionDataModel() }
    }

    fun updateQuestionAnswer(answerUpdateRequest: QuestionAnswerUpdateRequest): QuestionAnswerUpdateResponse {
        val response = QuestionAnswerUpdateResponse()
        try {
            val pulse = questionRepository.getPulseById(answerUpdateRequest.pulseId)
            pulse.pulseResult = answerUpdateRequest.pulseResult
            pulse.status = QuestionStatus.RESOLVED
            questionRepository.save(pulse)
            payoutService.processPayouts(pulse.mapToQuestionDataModel())
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
        val newQuestions = generateQuestions(currentState)

        previousState = currentState
        lastProcessedBallNumber = latestBallNumber

        return BallEventProcessResponse(
            updatedQuestions = updatedQuestionsResponse.updatedQuestions,
            notUpdatedQuestions = updatedQuestionsResponse.notUpdatedQuestions,
            newQuestions = newQuestions,
            errors = updatedQuestionsResponse.errors
        )
    }

    private fun updateQuestions(
        matchState: MatchState
    ): UpdateQuestionsResponse {
        val matchId = matchState.liveScorecard.matchId
        val questions = questionRepository.findByMatchIdInAndStatus(
            listOf(matchId), QuestionStatus.LIVE
        )?.map { it.mapToQuestionDataModel() } ?: listOf()

        val updatedQuestions = mutableListOf<QuestionDataModel>()
        val notUpdatedQuestions = mutableListOf<QuestionDataModel>()
        val errors = mutableListOf<QuestionError>()

        for (question in questions) {
            try {
                val resolutionStrategy = resolutionStrategies[question.questionType]
                if (resolutionStrategy?.canResolve(question, matchState) == true) {
                    val resolution = resolutionStrategy.resolve(question, matchState)
                    if (resolution.isResolved) {
                        question.pulseResult = resolution.result
                        question.status = QuestionStatus.RESOLVED
                        questionRepository.save(question.maptoEntity())
                        if (resolution.result != null) {
                            payoutService.processPayouts(question)
                        }
                        updatedQuestions.add(question)
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

    private fun generateQuestions(currentState: MatchState): List<QuestionDataModel> {
        return questionGenerators.flatMap { generator ->
            try {
                generator.generateQuestions(currentState, previousState)
            } catch (e: Exception) {
                logger.error("Error generating questions with ${generator::class.simpleName}", e)
                emptyList<QuestionDataModel>()
            }
        }.also { questions ->
            questionRepository.saveAll(questions.map { it.maptoEntity() })
        }
    }
}

data class QuestionError(
    val questionId: Int, val errorMessage: String
)

data class UpdateQuestionsResponse(
    val updatedQuestions: List<QuestionDataModel>,
    val notUpdatedQuestions: List<QuestionDataModel>,
    val errors: List<QuestionError>
)
package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.BallEventEntity
import com.hit11.zeus.model.Inning
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.request.QuestionAnswerUpdateRequest
import com.hit11.zeus.model.response.QuestionAnswerUpdateResponse
import com.hit11.zeus.oms.OrderService
import com.hit11.zeus.oms.TradeService
import com.hit11.zeus.question.QuestionHandlerFactory
import com.hit11.zeus.repository.BallEventRepository
import com.hit11.zeus.repository.BatsmanPerformanceRepository
import com.hit11.zeus.repository.BowlerPerformanceRepository
import com.hit11.zeus.repository.QuestionRepository
import org.springframework.stereotype.Service

@Service
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val payoutService: PayoutService
) {
    private val logger = Logger.getLogger(QuestionService::class.java)
    fun getAllActiveQuestions(matchIdList: List<Int>): List<QuestionDataModel>? {
        val activePulse = questionRepository.findByMatchIdInAndStatus(
            matchIdList,
            true
        )
        return activePulse?.map { it.mapToQuestionDataModel() }
    }

    fun updateQuestionAnswer(
        answerUpdateRequest: QuestionAnswerUpdateRequest
    ): QuestionAnswerUpdateResponse {
        val res = QuestionAnswerUpdateResponse()
        try {
            val pulse = questionRepository.getPulseById(answerUpdateRequest.pulseId)
            pulse.pulseResult = answerUpdateRequest.pulseResult
            pulse.status = false
            questionRepository.save(pulse)
            payoutService.processPayouts(pulse.mapToQuestionDataModel())
        } catch (e: Exception) {
            throw e
        }
        return res
    }

    fun updateQuestions(
        liveScorecard: Hit11Scorecard
    ): UpdateQuestionsResponse {
        val matchState = MatchState(liveScorecard)
        val questions = questionRepository.findByMatchIdInAndStatus(listOf(liveScorecard.matchId), true)
            ?.map { it.mapToQuestionDataModel() } ?: listOf()

        val updatedQuestions = mutableListOf<QuestionDataModel>()
        val notUpdatedQuestions = mutableListOf<QuestionDataModel>()
        val errors = mutableListOf<QuestionError>()

        for (question in questions) {
            try {
                val handler = QuestionHandlerFactory.getHandler(question)
                if (handler.canBeResolved(question, matchState)) {
                    val resolution = handler.resolveQuestion(question, matchState)
                    if (resolution.isResolved) {
                        question.pulseResult = resolution.result
                        question.enabled = false
                        questionRepository.save(question.maptoEntity())
                        payoutService.processPayouts(question)
                        updatedQuestions.add(question)
                    } else {
                        notUpdatedQuestions.add(question)
                    }
                } else {
                    notUpdatedQuestions.add(question)
                }
            } catch (e: QuestionValidationException) {
                errors.add(QuestionError(question.id, e.message ?: "Validation error"))
                notUpdatedQuestions.add(question)
            } catch (e: Exception) {
                errors.add(QuestionError(question.id, "Error processing question: ${e.message}"))
                notUpdatedQuestions.add(question)
            }
        }
        return UpdateQuestionsResponse(updatedQuestions, notUpdatedQuestions, errors)
    }

}

data class QuestionError(
    val questionId: Int,
    val errorMessage: String
)

data class UpdateQuestionsResponse(
    val updatedQuestions: List<QuestionDataModel>,
    val notUpdatedQuestions: List<QuestionDataModel>,
    val errors: List<QuestionError>
)
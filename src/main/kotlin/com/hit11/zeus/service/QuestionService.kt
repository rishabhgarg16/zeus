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
import com.hit11.zeus.question.RunsScoredByBatsmanQuestionGenerator
import com.hit11.zeus.question.TeamRunsInMatchQuestionGenerator
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
                // First, check if the question is outdated.
                if (question.status == QuestionStatus.LIVE && checkAndLockOutdatedQuestion(question, previousState,
                        matchState)) {
                    // If outdated, we mark it as DISABLED and do not process it further.
                    notUpdatedQuestions.add(question)
                    continue
                }

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

    private fun checkAndLockOutdatedQuestion(
        question: Question,
        previousState: MatchState? = null,
        currentState: MatchState,
    ): Boolean {
        when (question.questionType) {
            QuestionType.TEAM_RUNS_IN_MATCH -> {
                // TODO Super overs
                //DLS method affecting targets
                //Power play rule changes
                //Format-specific rules (T20 vs ODI)
                val generator = questionGenerators.firstOrNull { it is TeamRunsInMatchQuestionGenerator } as? TeamRunsInMatchQuestionGenerator
                    ?: return false
                val generatedParams = generator.parameterGenerator.generateParameters(currentState, previousState)

                // Find the candidate with the smallest difference in targetOvers from the stored question.
                val storedOvers = question.targetOvers ?: return false
                val candidate = generatedParams.minByOrNull { Math.abs(it.targetOvers - storedOvers) }

                // If no candidate is generated, we lock the question.
                if (candidate == null) {
                    question.status = QuestionStatus.DISABLED
                    questionRepository.save(question)
                    logger.info("Locked TEAM_RUNS_IN_MATCH question ${question.id} due to no candidate parameter found.")
                    return true
                }

                // Define tolerance thresholds.
                val oversTolerance = 1      // Allow a ±1 over difference.
                val runTolerance = 5       // Allow a ±5 runs difference.

                // Check if the target team has changed.
                if (candidate.targetTeamId != question.targetTeamId) {
                    question.status = QuestionStatus.DISABLED
                    questionRepository.save(question)
                    logger.info("Locked TEAM_RUNS_IN_MATCH question ${question.id} due to team change: candidate targetTeamId ${candidate.targetTeamId} vs stored ${question.targetTeamId}")
                    return true
                }

                // Check the difference in overs.
                val oversDiff = Math.abs(candidate.targetOvers - storedOvers)
                if (oversDiff > oversTolerance) {
                    question.status = QuestionStatus.DISABLED
                    questionRepository.save(question)
                    logger.info("Locked TEAM_RUNS_IN_MATCH question ${question.id} due to overs difference: candidate ${candidate.targetOvers}, stored $storedOvers")
                    return true
                }

                // Check the difference in runs.
                val storedRuns = question.targetRuns ?: 0
                val runDiff = Math.abs(candidate.targetRuns - storedRuns)
                if (runDiff > runTolerance) {
                    question.status = QuestionStatus.DISABLED
                    questionRepository.save(question)
                    logger.info("Locked TEAM_RUNS_IN_MATCH question ${question.id} due to runs difference: candidate ${candidate.targetRuns}, stored $storedRuns")
                    return true
                }

                // If all differences are within tolerance, the question remains active.
                return false
            }

            QuestionType.RUNS_SCORED_BY_BATSMAN -> {
                val generator = questionGenerators.firstOrNull { it is RunsScoredByBatsmanQuestionGenerator } as? RunsScoredByBatsmanQuestionGenerator
                    ?: return false
                val newParamsList = generator.parameterGenerator.generateParameters(currentState, previousState)
                // Here we try to match by targetBatsmanId; if found, compare targetRuns.
                val updatedParams = newParamsList.find { it.targetBatsmanId == question.targetBatsmanId }
                    ?: newParamsList.minByOrNull { Math.abs(it.targetRuns - (question.targetRuns ?: 0)) }
                val threshold = 10
                if (updatedParams != null && question.targetRuns != null) {
                    val runDifference = Math.abs(updatedParams.targetRuns - question.targetRuns!!)
                    if (runDifference > threshold) {
                        question.status = QuestionStatus.DISABLED
                        questionRepository.save(question)
                        logger.info("Locked RUNS_SCORED_BY_BATSMAN question ${question.id}: original targetRuns ${question.targetRuns}, updated targetRuns ${updatedParams.targetRuns}")
                        return true
                    }
                }
            }
            // Add more cases here for other question types (e.g., WicketsByBowler) using similar logic.
            else -> {
                // For other types, we may choose not to apply lock logic.
                return false
            }
        }
        return false
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
                generationErrors.add(QuestionError(questionId = -1, errorMessage = errorMsg))
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
package com.hit11.zeus.question

import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionStatus
import com.hit11.zeus.model.QuestionType
import com.hit11.zeus.repository.QuestionRepository
import java.time.Instant
import java.time.temporal.ChronoUnit

interface TriggerCondition {
    fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean
}

interface QuestionParameterGenerator<T : QuestionParameter> {
    fun generateParameters(currentState: MatchState, previousState: MatchState?): List<T>
}

interface QuestionGenerator<T : QuestionParameter> {
    val type: QuestionType
    val triggerCondition: TriggerCondition
    val parameterGenerator: QuestionParameterGenerator<T>
    val validator: QuestionValidator

    fun generateQuestions(currentState: MatchState, previousState: MatchState?): List<QuestionDataModel> {
        if (!triggerCondition.shouldTrigger(currentState, previousState)) {
            return emptyList()
        }

        val parameters = parameterGenerator.generateParameters(currentState, previousState)
        return parameters.mapNotNull { param ->
            if (!questionExists(param, currentState)) {
                createQuestion(param, currentState)?.takeIf { validator.validateQuestion(it) }
            } else {
                null
            }
        }
    }

    fun createQuestion(param: T, state: MatchState): QuestionDataModel?
    abstract fun questionExists(param: T, state: MatchState): Boolean
}

abstract class BaseQuestionGenerator<T : QuestionParameter>(
    protected val questionRepository: QuestionRepository
) : QuestionGenerator<T> {
    open fun calculateInitialWagers(param: T, state: MatchState): Pair<Long, Long> {
        // Default implementation: even odds
        return Pair(5L, 5L)
    }

    protected fun generateUserCounts(): Pair<Long, Long> {
        // Default implementation: random counts between 100 and 1000
        return Pair(
            (100..1000).random().toLong(),
            (100..1000).random().toLong()
        )
    }

    private fun getQuestionImage(): String? {
        // Default implementation: return null
        return null
    }

    // Helper method to create QuestionDataModel with default values
    protected fun createDefaultQuestionDataModel(
        matchId: Int,
        pulseQuestion: String,
        optionA: String,
        optionB: String,
        param: T,
        state: MatchState,
        category: List<String>,
        questionType: QuestionType,
        targetTeamId: Int? = null,
        targetRuns: Int? = null,
        targetOvers: Int? = null,
        targetBatsmanId: Int? = null,
        targetBowlerId: Int? = null,
        targetWickets: Int? = null,
        targetSixes: Int? = null,
        targetSpecificOver: Int? = null,
        targetExtras: Int? = null,
        targetWides: Int? = null,
        targetBoundaries: Int? = null,
        targetTossDecision: String? = null
    ): QuestionDataModel {
        val (wagerA, wagerB) = calculateInitialWagers(param, state)
        val (userACount, userBCount) = generateUserCounts()

        return QuestionDataModel(
            matchId = matchId,
            pulseQuestion = pulseQuestion,
            optionA = optionA,
            optionB = optionB,
            optionAWager = wagerA,
            optionBWager = wagerB,
            userACount = userACount,
            userBCount = userBCount,
            category = category,
            status = QuestionStatus.SYSTEM_GENERATED,
            questionType = questionType,
            targetTeamId = targetTeamId,
            targetRuns = targetRuns,
            targetOvers = targetOvers,
            targetBatsmanId = targetBatsmanId,
            targetBowlerId = targetBowlerId,
            targetWickets = targetWickets,
            targetSixes = targetSixes,
            targetSpecificOver = targetSpecificOver,
            targetExtras = targetExtras,
            targetWides = targetWides,
            targetBoundaries = targetBoundaries,
            targetTossDecision = targetTossDecision,
            pulseEndDate = Instant.now().plus(30, ChronoUnit.MINUTES),
            // TODO add pulseimage in question creation
            pulseImageUrl = getQuestionImage()
        )
    }
}

interface QuestionValidator {
    fun validateParameters(matchState: MatchState): Boolean
    fun validateQuestion(question: QuestionDataModel): Boolean
}

interface ResolutionStrategy {
    fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean
    fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution
}

data class QuestionResolution(
    val isResolved: Boolean,
    val result: String?
)
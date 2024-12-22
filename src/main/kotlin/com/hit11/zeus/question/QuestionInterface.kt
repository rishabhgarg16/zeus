package com.hit11.zeus.question

import com.hit11.zeus.model.*
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

    fun generateQuestions(currentState: MatchState, previousState: MatchState?): List<Question> {
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

    fun createQuestion(param: T, state: MatchState): Question?
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
    protected fun createDefaultQuestion(
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
    ): Question {
        val (wagerA, wagerB) = calculateInitialWagers(param, state)
        val (userACount, userBCount) = generateUserCounts()

        return Question(
            matchId = matchId,
            pulseQuestion = pulseQuestion,
            optionA = optionA,
            optionB = optionB,
            optionAWager = wagerA,
            optionBWager = wagerB,
            userACount = userACount,
            userBCount = userBCount,
            category = category.joinToString(", "),
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
    fun validateQuestion(question: Question): Boolean
}

interface ResolutionStrategy {
    fun canResolve(question: Question, matchState: MatchState): Boolean
    fun resolve(question: Question, matchState: MatchState): QuestionResolution
}

data class QuestionResolution(
    val isResolved: Boolean = false,
    val result: PulseResult = PulseResult.UNDECIDED
)
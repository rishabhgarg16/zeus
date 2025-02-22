package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository
import java.math.BigDecimal

data class MatchWinnerParameter(val targetTeamId: Int) : QuestionParameter()

class MatchWinnerTriggerCondition : TriggerCondition {
    // Map to track when a question was triggered for each match
    private val triggeredMatches = mutableMapOf<Int, Boolean>()

    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val matchId = currentState.liveScorecard.matchId

        // Only trigger if not already successfully triggered
        if (triggeredMatches[matchId] == true) {
            return false
        }

        // States in which it's acceptable to trigger the question
        val canBeTriggered = when (currentState.liveScorecard.state) {
            CricbuzzMatchPlayingState.SCHEDULED,
            CricbuzzMatchPlayingState.PREVIEW,
            CricbuzzMatchPlayingState.IN_PROGRESS,
            CricbuzzMatchPlayingState.INNINGS_BREAK,
            CricbuzzMatchPlayingState.TEA,
            CricbuzzMatchPlayingState.DRINK,
            CricbuzzMatchPlayingState.TOSS,
            CricbuzzMatchPlayingState.STUMPS -> true
            else -> false
        }

        if (canBeTriggered) {
            return true
        }

        return false
    }

    // Call this after question creation is successful to mark that the trigger is set
    fun markTriggered(matchId: Int) {
        triggeredMatches[matchId] = true
    }
}

class MatchWinnerParameterGenerator : QuestionParameterGenerator<MatchWinnerParameter> {
    override fun generateParameters(
        currentState: MatchState,
        previousState: MatchState?
    ): List<MatchWinnerParameter> {
        // question creation only for one team is sufficient
        return listOf(currentState.liveScorecard.team1.let { MatchWinnerParameter(it.id) })
    }
}

class MatchWinnerQuestionGenerator(
    questionRepository: QuestionRepository,
    override val triggerCondition: MatchWinnerTriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<MatchWinnerParameter>,
    override val validator: MatchWinnerQuestionValidator
) : BaseQuestionGenerator<MatchWinnerParameter>(questionRepository) {
    override val type = QuestionType.MATCH_WINNER

    override fun questionExists(param: MatchWinnerParameter, state: MatchState): Boolean {
        val matchId = state.liveScorecard.matchId
        val questionsExists = questionRepository.existsByMatchIdAndQuestionTypeAndTargetTeamId(
            matchId,
            QuestionType.MATCH_WINNER,
            param.targetTeamId
        )

        if (questionsExists) {
            triggerCondition.markTriggered(matchId)
        }

        return questionsExists
    }

    override fun createQuestion(param: MatchWinnerParameter, state: MatchState): Question? {
        val teams = listOf(state.liveScorecard.team1, state.liveScorecard.team2)
        val team = teams.find { it.id == param.targetTeamId } ?: return null
        val (wagerA, wagerB) = calculateInitialWagers(param, state)
        val question = createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${team.name} win the match?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
            category = listOf("Match Result"),
            questionType = QuestionType.MATCH_WINNER,
            targetTeamId = param.targetTeamId,
            param = param,
            state = state
        )

        // If question is created successfully, mark the match as triggered.
        triggerCondition.markTriggered(state.liveScorecard.matchId)
        return question
    }

    override fun calculateInitialWagers(param: MatchWinnerParameter, state: MatchState): Pair<BigDecimal, BigDecimal> {
        return Pair(BigDecimal(5), BigDecimal(5))
    }
}

class MatchWinnerQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: Question): Boolean {
        if (question.questionType != QuestionType.MATCH_WINNER) {
            return false
        }
        if (question.targetTeamId == null || question.targetTeamId == 0) {
            throw QuestionValidationException("Target team must be specified for Match Winner question")
        }
        if (question.optionA.isBlank() || question.optionB.isBlank()) {
            throw QuestionValidationException("Both team options must be provided for Match Winner question")
        }
        return true
    }

    override fun validateParameters(matchState: MatchState): Boolean {
        return true
    }
}

class MatchWinnerResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: Question, matchState: MatchState): Boolean {
        // Allow resolution if the match is complete OR if result is available
        return matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE ||
                (matchState.liveScorecard.result != null && matchState.liveScorecard.result.winningTeamId > 0)
    }


    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val winningTeamId = matchState.liveScorecard.result?.winningTeamId
        val result = if (winningTeamId == question.targetTeamId) PulseResult.Yes else PulseResult.No
        return QuestionResolution(true, result)
    }
}
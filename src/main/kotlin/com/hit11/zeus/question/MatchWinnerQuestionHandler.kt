package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository

data class MatchWinnerParameter(val targetTeamId: Int) : QuestionParameter()

class MatchWinnerTriggerCondition : TriggerCondition {
    private var hasTriggered = false

    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        // we just need match winner question only once
        if (hasTriggered) {
            return false
        }

        if (currentState.liveScorecard.state == CricbuzzMatchPlayingState.IN_PROGRESS) {
            hasTriggered = true
            return true
        }

        return false
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
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<MatchWinnerParameter>,
    override val validator: MatchWinnerQuestionValidator
) : BaseQuestionGenerator<MatchWinnerParameter>(questionRepository) {
    override val type = QuestionType.MATCH_WINNER

    override fun questionExists(param: MatchWinnerParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetTeamId(
            state.liveScorecard.matchId,
            QuestionType.MATCH_WINNER.text,
            param.targetTeamId
        )
    }

    override fun createQuestion(param: MatchWinnerParameter, state: MatchState): QuestionDataModel? {
        val teams = listOf(state.liveScorecard.team1, state.liveScorecard.team2)
        val team = teams.find { it.id == param.targetTeamId } ?: return null
        val (wagerA, wagerB) = calculateInitialWagers(param, state)
        return createDefaultQuestionDataModel(
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
    }

    override fun calculateInitialWagers(param: MatchWinnerParameter, state: MatchState): Pair<Long, Long> {
        return Pair(5L, 5L)
    }
}

class MatchWinnerQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: QuestionDataModel): Boolean {
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
    override fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean {
        return (matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE ||
                matchState.liveScorecard.result != null && matchState.liveScorecard.result.winningTeamId > 0)
    }


    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val result = if (matchState.liveScorecard.result?.winningTeamId == question.targetTeamId) PulseResult.Yes else PulseResult.No
        return QuestionResolution(true, result)
    }
}
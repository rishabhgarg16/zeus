package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.CricbuzzMatchPlayingState
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionType

data class TossWinnerParameter(val targetTeamId: Int) : QuestionParameter()

class TossWinnerTriggerCondition : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        // TODO check toss decision question state
        return currentState.liveScorecard.state == CricbuzzMatchPlayingState.PREVIEW
    }
}

class TossWinnerParameterGenerator : QuestionParameterGenerator<TossWinnerParameter> {
    override fun generateParameters(currentState: MatchState, previousState: MatchState?): List<TossWinnerParameter> {
        // one question parameter is enough
        return listOf(
            TossWinnerParameter(currentState.liveScorecard.team1.id)
        )
    }
}

class TossWinnerQuestionGenerator(
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<TossWinnerParameter>,
    override val validator: TossWinnerQuestionValidator
) : BaseQuestionGenerator<TossWinnerParameter>() {
    override val type = QuestionType.TOSS_WINNER

    override fun createQuestion(param: TossWinnerParameter, state: MatchState): QuestionDataModel? {
        val teams = listOf(state.liveScorecard.team1, state.liveScorecard.team2)
        val team = teams.find { it.id == param.targetTeamId } ?: return null
        return createDefaultQuestionDataModel(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${team.name} win the toss?",
            optionA = "Yes",
            optionB = "No",
            category = listOf("Toss"),
            questionType = QuestionType.TOSS_WINNER,
            targetTeamId = param.targetTeamId,
            param = param,
            state = state
        )
    }
}

class TossWinnerQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: QuestionDataModel): Boolean {
        if (question.questionType != QuestionType.TOSS_WINNER) {
            return false
        }
        if (question.targetTeamId == null || question.targetTeamId == 0) {
            throw QuestionValidationException("Target team must be specified for Toss Winner question")
        }
        return true
    }

    override fun validateParameters(matchState: MatchState): Boolean {
        TODO("Not yet implemented")
    }
}

class TossWinnerResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean =
        matchState.liveScorecard.tossResult != null

    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val tossResult = matchState.liveScorecard.tossResult ?: return QuestionResolution(false, null)
        val result = if (tossResult.tossWinnerTeamId == question.targetTeamId) "Yes" else "No"
        return QuestionResolution(true, result)
    }
}
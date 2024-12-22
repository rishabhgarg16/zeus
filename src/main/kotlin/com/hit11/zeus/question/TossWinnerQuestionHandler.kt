package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository

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
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<TossWinnerParameter>,
    override val validator: TossWinnerQuestionValidator
) : BaseQuestionGenerator<TossWinnerParameter>(questionRepository) {
    override val type = QuestionType.TOSS_WINNER

    override fun questionExists(param: TossWinnerParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionType(
            state.liveScorecard.matchId,
            QuestionType.TOSS_WINNER
        )
    }

    override fun createQuestion(param: TossWinnerParameter, state: MatchState): Question? {
        val teams = listOf(state.liveScorecard.team1, state.liveScorecard.team2)
        val team = teams.find { it.id == param.targetTeamId } ?: return null
        return createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${team.name} win the toss?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
            category = listOf("Toss"),
            questionType = QuestionType.TOSS_WINNER,
            targetTeamId = param.targetTeamId,
            param = param,
            state = state
        )
    }
}

class TossWinnerQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: Question): Boolean {
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
    override fun canResolve(question: Question, matchState: MatchState): Boolean =
        matchState.liveScorecard.tossResult != null

    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val tossResult = matchState.liveScorecard.tossResult ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val result = if (tossResult.tossWinnerTeamId == question.targetTeamId) PulseResult.Yes else PulseResult.No
        return QuestionResolution(true, result)
    }
}
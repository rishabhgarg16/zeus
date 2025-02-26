package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository

data class TotalExtrasParameter(
    val targetTeamId: Int,
    val targetExtras: Int
) : QuestionParameter()

class TotalExtrasTriggerCondition(private val triggerEveryNExtras: Int) : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
//        if (currentState.liveScorecard.state == CricbuzzMatchPlayingState.IN_PROGRESS) {
//            val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
//            val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }
//
//            val currentExtras = currentInnings?.totalExtras ?: 0
//            val previousExtras = previousInnings?.totalExtras ?: 0
//
//            return currentExtras != previousExtras && currentExtras % triggerEveryNExtras == 0
//        }

        // we do not extras information in the criccbuzz api
        return false
    }
}

class TotalExtrasParameterGenerator : QuestionParameterGenerator<TotalExtrasParameter> {
    override fun generateParameters(currentState: MatchState, previousState: MatchState?): List<TotalExtrasParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings } ?: return emptyList()
        val currentExtras = currentInnings.totalExtras
        val targetExtras = ((currentExtras / 3) + 1) * 3

        // TODO check later as extra question is not important
        return listOf(
            TotalExtrasParameter(
                currentInnings.bowlingTeam!!.id,
                targetExtras
            )
        )
    }
}

class TotalExtrasQuestionGenerator(
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<TotalExtrasParameter>,
    override val validator: TotalExtrasQuestionValidator
) : BaseQuestionGenerator<TotalExtrasParameter>(questionRepository) {
    override val type = QuestionType.TOTAL_EXTRAS

    override fun questionExists(param: TotalExtrasParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetTeamIdAndTargetExtras(
            state.liveScorecard.matchId,
            QuestionType.TOTAL_EXTRAS,
            param.targetTeamId,
            param.targetExtras
        )
    }

    override fun createQuestion(param: TotalExtrasParameter, state: MatchState): Question? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null
        val team = state.liveScorecard.innings.map { it.bowlingTeam }.find {
            it?.id == param.targetTeamId
        } ?: return null
        return createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${team.name} concede ${param.targetExtras} or more extras in this innings?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
            category = listOf("Bowling"),
            questionType = QuestionType.TOTAL_EXTRAS,
            targetTeamId = param.targetTeamId,
            targetExtras = param.targetExtras,
            param = param,
            state = state
        )
    }
}

class TotalExtrasQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: Question): Boolean {
        if (question.questionType != QuestionType.TOTAL_EXTRAS) {
            return false
        }
        if (question.targetExtras == null || question.targetExtras <= 0) {
            throw QuestionValidationException("Target extras must be a positive number for question ${question.id}")
        }
        if (question.targetTeamId == null) {
            throw QuestionValidationException("Target team ID is required for question ${question.id}")
        }
        return true
    }

    override fun validateParameters(matchState: MatchState): Boolean {
        TODO("Not yet implemented")
    }
}

class TotalExtrasResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: Question, matchState: MatchState): Boolean {
        val targetTeamId = question.targetTeamId ?: return false
        val targetExtras = question.targetExtras ?: return false
        val currentInnings = matchState.liveScorecard.innings.find { it.bowlingTeam?.id == targetTeamId }

        return currentInnings != null &&
                (currentInnings.totalExtras >= targetExtras ||
                        matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE)
    }

    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val targetTeamId = question.targetTeamId ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetExtras = question.targetExtras ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetInnings = matchState.liveScorecard.innings.find { it.bowlingTeam?.id == targetTeamId }

        val result = targetInnings?.totalExtras?.let { it >= targetExtras } ?: false
        return QuestionResolution(true, if (result) PulseResult.Yes else PulseResult.No)
    }
}
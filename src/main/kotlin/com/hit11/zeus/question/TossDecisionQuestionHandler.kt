package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.CricbuzzMatchPlayingState
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionType

data class TossDecisionParameter(val targetDecision: String) : QuestionParameter()

class TossDecisionTriggerCondition : TriggerCondition {
    private var hasTriggered = false
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        // should be triggered only once
        if (hasTriggered) {
            return false
        }

        // TODO check match state before toss to generate this question
        if (currentState.liveScorecard.state == CricbuzzMatchPlayingState.PREVIEW) {
            hasTriggered = true
            return true
        }
        return false
    }
}

class TossDecisionParameterGenerator : QuestionParameterGenerator<TossDecisionParameter> {
    override fun generateParameters(
        currentState: MatchState,
        previousState: MatchState?
    ): List<TossDecisionParameter> {
        return listOf(
            TossDecisionParameter("Bat")
        )
    }
}

class TossDecisionQuestionGenerator(
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<TossDecisionParameter>,
    override val validator: TossDecisionQuestionValidator
) : BaseQuestionGenerator<TossDecisionParameter>() {
    override val type = QuestionType.TOSS_DECISION

    override fun createQuestion(param: TossDecisionParameter, state: MatchState): QuestionDataModel {
        return createDefaultQuestionDataModel(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will the toss winner choose to ${param.targetDecision}?",
            optionA = "Yes",
            optionB = "No",
            category = listOf("Toss"),
            questionType = QuestionType.TOSS_DECISION,
            targetTossDecision = param.targetDecision,
            param = param,
            state = state
        )
    }
}

class TossDecisionQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: QuestionDataModel): Boolean {
        if (question.questionType != QuestionType.TOSS_DECISION) {
            return false
        }
        if (question.targetTossDecision.isNullOrBlank()) {
            throw QuestionValidationException("Target decision must be specified for Toss Decision question")
        }
        return true
    }

    override fun validateParameters(matchState: MatchState): Boolean {
        TODO("Not yet implemented")
    }
}

class TossDecisionResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean =
        matchState.liveScorecard.tossResult != null

    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val tossResult = matchState.liveScorecard.tossResult ?: return QuestionResolution(false, null)
        val result = if (question.targetTossDecision.equals(tossResult.tossDecision, ignoreCase = true)) "Yes" else "No"
        return QuestionResolution(true, result)
    }
}
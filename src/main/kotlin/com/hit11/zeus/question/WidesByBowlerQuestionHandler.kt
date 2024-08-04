package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionType

data class WidesByBowlerParameter(
    val targetBowlerId: Int,
    val targetWides: Int
) : QuestionParameter()

class WidesByBowlerTriggerCondition(private val triggerEveryNWides: Int) : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }

        val currentWides = currentInnings?.bowlingPerformances?.sumOf { it.wides } ?: 0
        val previousWides = previousInnings?.bowlingPerformances?.sumOf { it.wides } ?: 0

        return currentWides % triggerEveryNWides == 0 && currentWides != previousWides
    }
}

class WidesByBowlerParameterGenerator : QuestionParameterGenerator<WidesByBowlerParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<WidesByBowlerParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings } ?: return emptyList()
        return currentInnings.bowlingPerformances.map {
            WidesByBowlerParameter(it.playerId, it.wides + 2)
        }
    }
}

class WidesByBowlerQuestionGenerator(
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<WidesByBowlerParameter>,
    override val validator: WidesByBowlerQuestionValidator
) : BaseQuestionGenerator<WidesByBowlerParameter>() {
    override val type = QuestionType.WIDES_BY_BOWLER

    override fun createQuestion(param: WidesByBowlerParameter, state: MatchState): QuestionDataModel? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null
        val bowler = currentInnings.bowlingPerformances.find { it.playerId == param.targetBowlerId } ?: return null
        return createDefaultQuestionDataModel(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${bowler.playerName} bowl ${param.targetWides} or more wides in this innings?",
            optionA = "Yes",
            optionB = "No",
            category = listOf("Bowling"),
            questionType = QuestionType.WIDES_BY_BOWLER,
            targetBowlerId = param.targetBowlerId,
            targetWides = param.targetWides,
            param = param,
            state = state
        )
    }
}

class WidesByBowlerQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: QuestionDataModel): Boolean {
        if (question.questionType != QuestionType.WIDES_BY_BOWLER) {
            return false
        }
        if (question.targetBowlerId == null) {
            throw QuestionValidationException("Target bowler ID is required for Wides by Bowler question")
        }
        if (question.targetWides == null || question.targetWides <= 0) {
            throw QuestionValidationException("Target wides must be a positive number for Wides by Bowler question")
        }
        return true
    }

    override fun validateParameters(matchState: MatchState): Boolean {
        TODO("Not yet implemented")
    }
}

class WidesByBowlerResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean {
        return true // This can be resolved at any time during the match
    }

    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetBowlerId = question.targetBowlerId ?: return QuestionResolution(false, null)
        val targetWides = question.targetWides ?: return QuestionResolution(false, null)

        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val bowlerPerformance = currentInnings?.bowlingPerformances?.find { it.playerId == targetBowlerId }

        if (bowlerPerformance == null) {
            return QuestionResolution(false, null)
        }

        val widesBowled = bowlerPerformance.wides
        val result = if (widesBowled >= targetWides) "Yes" else "No"
        return QuestionResolution(true, result)
    }
}
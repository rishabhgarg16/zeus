package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionType

data class SixesByPlayerParameter(
    val batsmanId: Long, val targetSixes: Int
) : QuestionParameter()

class SixesByPlayerTriggerCondition : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }

        // Find the batsman who hit a new six
        return currentInnings?.battingPerformances?.any { currentBatsman ->
            val previousBatsman = previousInnings?.battingPerformances?.find { it.playerId == currentBatsman.playerId }
            currentBatsman.sixes > (previousBatsman?.sixes ?: 0)
        } ?: false
    }
}

class SixesByPlayerParameterGenerator : QuestionParameterGenerator<SixesByPlayerParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<SixesByPlayerParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings } ?: return emptyList()
        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }

        // Find the batsman who just hit a six
        val batsmanWithNewSix = currentInnings.battingPerformances.find { currentBatsman ->
            val previousBatsman = previousInnings?.battingPerformances?.find { it.playerId == currentBatsman.playerId }
            currentBatsman.sixes > (previousBatsman?.sixes ?: 0)
        } ?: return emptyList()

        return listOf(
            SixesByPlayerParameter(
                batsmanId = batsmanWithNewSix.playerId.toLong(),
                targetSixes = batsmanWithNewSix.sixes + 1 // Target is current sixes + 1
            )
        )
    }
}

class SixesByPlayerQuestionGenerator(
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<SixesByPlayerParameter>,
    override val validator: SixesByPlayerQuestionValidator
) : BaseQuestionGenerator<SixesByPlayerParameter>() {
    override val type = QuestionType.SIX_BY_PLAYER

    override fun createQuestion(
        param: SixesByPlayerParameter, state: MatchState
    ): QuestionDataModel? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null
        val batsman = currentInnings.battingPerformances.find { it.playerId == param.batsmanId.toInt() } ?: return null
        return createDefaultQuestionDataModel(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${batsman.playerName} hit ${param.targetSixes} or more sixes in this innings?",
            optionA = "Yes",
            optionB = "No",
            category = listOf("Batting"),
            questionType = QuestionType.SIX_BY_PLAYER,
            targetBatsmanId = param.batsmanId.toInt(),
            targetSixes = param.targetSixes,
            param = param,
            state = state
        )
    }

     override fun calculateInitialWagers(param: SixesByPlayerParameter, state: MatchState): Pair<Long, Long> {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return Pair(5L, 5L)
        val batsman =
            currentInnings.battingPerformances.find { it.playerId == param.batsmanId.toInt() } ?: return Pair(5L, 5L)

        val currentSixes = batsman.sixes
        val ballsFaced = batsman.balls
        val sixRate = if (ballsFaced > 0) currentSixes.toFloat() / ballsFaced else 0f

        val probability = when {
            sixRate > 0.2 -> 0.8 // Very high six rate
            sixRate > 0.15 -> 0.7
            sixRate > 0.1 -> 0.6
            sixRate > 0.05 -> 0.5
            else -> 0.4
        }

        val wagerA = (10 * probability).toLong().coerceIn(1L, 9L)
        val wagerB = 10 - wagerA

        return Pair(wagerA, wagerB)
    }
}

class SixesByPlayerQuestionValidator : QuestionValidator {
    override fun validateParameters(matchState: MatchState): Boolean {
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        return currentInnings != null && currentInnings.battingPerformances.any { it.sixes > 0 }
    }

    override fun validateQuestion(question: QuestionDataModel): Boolean {
        if (question.questionType != QuestionType.SIX_BY_PLAYER) {
            return false
        }
        if (question.targetBatsmanId == null || question.targetBatsmanId <= 0) {
            throw QuestionValidationException(
                "Target batsman ID is required for Sixes by Player question ${question.id}"
            )
        }
        if (question.targetSixes == null || question.targetSixes <= 0) {
            throw QuestionValidationException(
                "Target sixes must be a positive number for Sixes by Player question ${question.id}"
            )
        }
        return true
    }
}

class SixesByPlayerResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean {
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val batsmanPerformance = currentInnings?.battingPerformances?.find { it.playerId == question.targetBatsmanId }

        return batsmanPerformance != null &&
                (batsmanPerformance.sixes >= (question.targetSixes ?: 0) ||
                        batsmanPerformance.outDescription != null ||
                        !currentInnings.isCurrentInnings)
    }

    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetSixes = question.targetSixes ?: return QuestionResolution(false, null)
        val targetBatsmanId = question.targetBatsmanId ?: return QuestionResolution(false, null)
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val currentSixes = currentInnings?.battingPerformances?.find { it.playerId == targetBatsmanId }?.sixes ?: 0

        val result = if (currentSixes >= targetSixes) "Yes" else "No"
        return QuestionResolution(true, result)
    }
}



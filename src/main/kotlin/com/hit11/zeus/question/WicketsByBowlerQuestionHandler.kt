package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*

data class WicketsByBowlerParameter(
    val targetBowlerId: Int,
    val targetWickets: Int
) : QuestionParameter()

class WicketsByBowlerTriggerCondition(triggerEveryNWickets: Int) : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }

        // Trigger when a wicket falls
        // TODO check which player took the wicket and check against that bowler
        return (currentInnings?.wickets ?: 0) > (previousInnings?.wickets ?: 0)
    }
}

class WicketsByBowlerParameterGenerator : QuestionParameterGenerator<WicketsByBowlerParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<WicketsByBowlerParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings } ?: return emptyList()
        return currentInnings.bowlingPerformances.map {
            WicketsByBowlerParameter(it.playerId, it.wickets + 1)
        }
    }
}

class WicketsByBowlerQuestionGenerator(
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<WicketsByBowlerParameter>,
    override val validator: WicketsByBowlerQuestionValidator
) : BaseQuestionGenerator<WicketsByBowlerParameter>() {
    override val type = QuestionType.WICKETS_BY_BOWLER

    override fun createQuestion(param: WicketsByBowlerParameter, state: MatchState): QuestionDataModel? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null
        val bowler = currentInnings.bowlingPerformances.find { it.playerId == param.targetBowlerId } ?: return null

        return createDefaultQuestionDataModel(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${bowler.playerName} take ${param.targetWickets} or more wickets in this innings?",
            optionA = "Yes",
            optionB = "No",
            category = listOf("Bowling"),
            questionType = QuestionType.WICKETS_BY_BOWLER,
            targetBowlerId = param.targetBowlerId,
            targetWickets = param.targetWickets,
            param = param,
            state = state
        )
    }

    override fun calculateInitialWagers(param: WicketsByBowlerParameter, state: MatchState): Pair<Long, Long> {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return Pair(5L, 5L)
        val matchFormat = state.liveScorecard.matchFormat
        val bowler =
            currentInnings.bowlingPerformances.find { it.playerId == param.targetBowlerId } ?: return Pair(5L, 5L)

        val currentWickets = bowler.wickets
        val targetWickets = param.targetWickets
        val remainingWickets = targetWickets - currentWickets
        // TODO check total overs definition from the match state
        val remainingOvers = (if (matchFormat == MatchFormat.ODI) 50 else 20) - currentInnings.overs.toInt()

        val probability = when {
            remainingWickets <= 0 -> 0.9
            remainingOvers == 0 -> 0.1
            remainingWickets == 1 -> 0.6
            remainingWickets == 2 -> 0.4
            else -> 0.2
        }

        val wagerA = (10 * probability).toLong().coerceIn(1L, 9L)
        val wagerB = 10 - wagerA

        return Pair(wagerA, wagerB)
    }
}

class WicketsByBowlerQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: QuestionDataModel): Boolean {
        if (question.questionType != QuestionType.WICKETS_BY_BOWLER) {
            return false
        }
        if (question.targetBowlerId == null) {
            throw QuestionValidationException("Target bowler ID is required for Wickets by Bowler question")
        }
        if (question.targetWickets == null || question.targetWickets <= 0) {
            throw QuestionValidationException("Target wickets must be a positive number for Wickets by Bowler question")
        }
        return true
    }

    override fun validateParameters(matchState: MatchState): Boolean {
        TODO("Not yet implemented")
    }
}

class WicketsByBowlerResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean {
        val targetBowlerId = question.targetBowlerId ?: return false
        val targetWickets = question.targetWickets ?: return false
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val bowlerPerformance = currentInnings?.bowlingPerformances?.find { it.playerId == targetBowlerId }

        return bowlerPerformance != null &&
                (bowlerPerformance.wickets >= targetWickets ||
                        // match has been completed
                        matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE)
    }

    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetBowlerId = question.targetBowlerId ?: return QuestionResolution(false, null)
        val targetWickets = question.targetWickets ?: return QuestionResolution(false, null)
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val bowlerPerformance = currentInnings?.bowlingPerformances?.find { it.playerId == targetBowlerId }

        val result = bowlerPerformance?.wickets?.let { it >= targetWickets } ?: false
        return QuestionResolution(true, if (result) "Yes" else "No")
    }

}
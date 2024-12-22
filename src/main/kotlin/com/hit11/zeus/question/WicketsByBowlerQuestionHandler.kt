package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository

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
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<WicketsByBowlerParameter>,
    override val validator: WicketsByBowlerQuestionValidator
) : BaseQuestionGenerator<WicketsByBowlerParameter>(questionRepository) {
    override val type = QuestionType.WICKETS_BY_BOWLER

    override fun questionExists(param: WicketsByBowlerParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetBowlerIdAndTargetWickets(
            state.liveScorecard.matchId,
            QuestionType.WICKETS_BY_BOWLER,
            param.targetBowlerId,
            param.targetWickets
        )
    }
    override fun createQuestion(param: WicketsByBowlerParameter, state: MatchState): Question? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null
        val bowler = currentInnings.bowlingPerformances.find { it.playerId == param.targetBowlerId } ?: return null

        return createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${bowler.playerName} take ${param.targetWickets} or more wickets in this innings?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
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
    override fun validateQuestion(question: Question): Boolean {
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
    override fun canResolve(question: Question, matchState: MatchState): Boolean {
        val targetBowlerId = question.targetBowlerId ?: return false
        val targetWickets = question.targetWickets ?: return false

        // Find the relevant innings (current or completed) where this bowler bowled
        val relevantInnings = matchState.liveScorecard.innings.find { innings ->
            innings.bowlingPerformances.any { it.playerId == targetBowlerId }
        }

        // Check if the bowler's performance can be found
        val bowlerPerformance = relevantInnings?.bowlingPerformances?.find { it.playerId == targetBowlerId }

        return bowlerPerformance != null && (
                bowlerPerformance.wickets >= targetWickets ||  // Target achieved
                        !relevantInnings.isCurrentInnings ||  // Innings has ended
                        matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE  // Match has ended
                )
    }

    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val targetBowlerId = question.targetBowlerId ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetWickets = question.targetWickets ?: return QuestionResolution(false, PulseResult.UNDECIDED)

        // Find the relevant innings where this bowler bowled
        val relevantInnings = matchState.liveScorecard.innings.find { innings ->
            innings.bowlingPerformances.any { it.playerId == targetBowlerId }
        }
        val bowlerPerformance = relevantInnings?.bowlingPerformances?.find { it.playerId == targetBowlerId }

        if (bowlerPerformance == null) {
            return QuestionResolution(false, PulseResult.UNDECIDED)
        }

        val wicketsTaken = bowlerPerformance.wickets
        val result = if (wicketsTaken >= targetWickets) PulseResult.Yes else PulseResult.No

        // Always resolve if the innings or match has ended
        return QuestionResolution(true, result)
    }


}
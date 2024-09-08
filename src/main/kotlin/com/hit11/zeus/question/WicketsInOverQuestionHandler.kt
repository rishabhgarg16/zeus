package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository

data class WicketsInOverParameter(val targetBowlerId: Int, val targetOver: Int, val targetWickets: Int) :
    QuestionParameter()

class WicketsInOverTriggerCondition : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }

        return currentInnings?.overs?.toInt() != previousInnings?.overs?.toInt()
    }
}

class WicketsInOverParameterGenerator : QuestionParameterGenerator<WicketsInOverParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<WicketsInOverParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings } ?: return emptyList()
        val currentOver = currentInnings.overs.toInt()
        val bowler = currentInnings.bowlingPerformances.find { it.onStrike == 1 } ?: return emptyList()

        return listOf(
            WicketsInOverParameter(bowler.playerId, currentOver + 1, 1),
            WicketsInOverParameter(bowler.playerId, currentOver + 1, 2)
        )
    }
}

class WicketsInOverQuestionGenerator(
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<WicketsInOverParameter>,
    override val validator: WicketsInOverQuestionValidator
) : BaseQuestionGenerator<WicketsInOverParameter>(questionRepository) {
    override val type = QuestionType.WICKETS_IN_OVER

    override fun questionExists(param: WicketsInOverParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetBowlerIdAndTargetWicketsAndTargetSpecificOver(
            state.liveScorecard.matchId,
            QuestionType.WICKETS_IN_OVER,
            param.targetBowlerId,
            param.targetWickets,
            param.targetOver
        )
    }

    override fun createQuestion(param: WicketsInOverParameter, state: MatchState): QuestionDataModel? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null
        val bowler = currentInnings.bowlingPerformances.find { it.playerId == param.targetBowlerId } ?: return null
        return createDefaultQuestionDataModel(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${bowler.playerName} take ${param.targetWickets} or more wickets in the ${param.targetOver}th over?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
            category = listOf("Bowling"),
            questionType = QuestionType.WICKETS_IN_OVER,
            targetBowlerId = param.targetBowlerId,
            targetWickets = param.targetWickets,
            targetSpecificOver = param.targetOver,
            param = param,
            state = state
        )
    }
}

class WicketsInOverQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: QuestionDataModel): Boolean {
        if (question.questionType != QuestionType.WICKETS_IN_OVER) {
            return false
        }
        if (question.targetBowlerId == null) {
            throw QuestionValidationException("Target bowler ID is required for Wickets in Over question")
        }
        if (question.targetWickets == null || question.targetWickets <= 0) {
            throw QuestionValidationException("Target wickets must be a positive number for Wickets in Over question")
        }
        if (question.targetSpecificOver == null || question.targetSpecificOver < 0) {
            throw QuestionValidationException("Target over must be a non-negative number for Wickets in Over question")
        }
        return true
    }

    override fun validateParameters(matchState: MatchState): Boolean {
        TODO("Not yet implemented")
    }
}

class WicketsInOverResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean {
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val currentOver = currentInnings?.overs?.toInt() ?: -1
        return currentOver > (question.targetSpecificOver ?: -1)
    }

    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetOver = question.targetSpecificOver ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetWickets = question.targetWickets ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetBowlerId = question.targetBowlerId ?: return QuestionResolution(false, PulseResult.UNDECIDED)

        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val wicketInTargetOver = currentInnings?.ballByBallEvents
            ?.filter { it.overNumber == targetOver }
            ?.any { it.isWicket } ?: false

        return QuestionResolution(true, if (wicketInTargetOver) PulseResult.Yes else PulseResult.No)
    }
}

private fun countWicketsInOver(scorecard: Hit11Scorecard, targetBowlerId: Int, targetOver: Int): Int {
    // Implement logic to count wickets in the specific over
    // This would involve iterating through the ball-by-ball events of the scorecard
    // and counting wickets for the specific bowler in the target over
    // Return the count of wickets
    return 0 // Placeholder return

}
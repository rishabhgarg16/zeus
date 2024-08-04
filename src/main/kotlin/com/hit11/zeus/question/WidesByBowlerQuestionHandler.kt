package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.CricbuzzMatchPlayingState
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionType
import com.hit11.zeus.repository.QuestionRepository

data class WidesByBowlerParameter(
    val targetBowlerId: Int,
    val targetWides: Int
) : QuestionParameter()

class WidesByBowlerTriggerCondition : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }

        return currentInnings?.bowlingPerformances?.any { currentBowler ->
            val previousBowler = previousInnings?.bowlingPerformances?.find { it.playerId == currentBowler.playerId }
            currentBowler.wides > (previousBowler?.wides ?: 0)
        } ?: false
    }
}

class WidesByBowlerParameterGenerator : QuestionParameterGenerator<WidesByBowlerParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<WidesByBowlerParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings } ?: return emptyList()
        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }

        return currentInnings.bowlingPerformances.mapNotNull { currentBowler ->
            val previousBowler = previousInnings?.bowlingPerformances?.find { it.playerId == currentBowler.playerId }
            if (currentBowler.wides > (previousBowler?.wides ?: 0)) {
                WidesByBowlerParameter(currentBowler.playerId, currentBowler.wides + 2)
            } else null
        }
    }
}

class WidesByBowlerQuestionGenerator(
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<WidesByBowlerParameter>,
    override val validator: WidesByBowlerQuestionValidator
) : BaseQuestionGenerator<WidesByBowlerParameter>(questionRepository) {
    override val type = QuestionType.WIDES_BY_BOWLER

    override fun questionExists(param: WidesByBowlerParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetBowlerIdAndTargetWides(
            state.liveScorecard.matchId,
            QuestionType.WIDES_BY_BOWLER.text,
            param.targetBowlerId,
            param.targetWides
        )
    }

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
        val targetBowlerId = question.targetBowlerId ?: return false
        val targetWides = question.targetWides ?: return false

        // Find the relevant innings (current or completed) where this bowler bowled
        val relevantInnings = matchState.liveScorecard.innings.find { innings ->
            innings.bowlingPerformances.any { it.playerId == targetBowlerId }
        }

        // Check if the bowler's performance can be found
        val bowlerPerformance = relevantInnings?.bowlingPerformances?.find { it.playerId == targetBowlerId }

        return bowlerPerformance != null && (
                bowlerPerformance.wides >= targetWides ||  // Target achieved
                        !relevantInnings.isCurrentInnings ||  // Innings has ended
                        matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE  // Match has ended
                )
    }

    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetBowlerId = question.targetBowlerId ?: return QuestionResolution(false, null)
        val targetWides = question.targetWides ?: return QuestionResolution(false, null)

        // Find the relevant innings where this bowler bowled
        val relevantInnings = matchState.liveScorecard.innings.find { innings ->
            innings.bowlingPerformances.any { it.playerId == targetBowlerId }
        }
        val bowlerPerformance = relevantInnings?.bowlingPerformances?.find { it.playerId == targetBowlerId }

        if (bowlerPerformance == null) {
            return QuestionResolution(false, null)
        }

        val widesBowled = bowlerPerformance.wides
        val result = if (widesBowled >= targetWides) "Yes" else "No"

        // Always resolve if the innings or match has ended
        return QuestionResolution(true, result)
    }
}
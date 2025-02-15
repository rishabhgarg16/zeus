package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository
import java.math.BigDecimal

data class SixesByPlayerParameter(
    val batsmanId: Long, val targetSixes: Int
) : QuestionParameter()

class SixesByPlayerTriggerCondition : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
//        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
//        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }
//
//        // Find the batsman who hit a new six
//        return currentInnings?.battingPerformances?.any { currentBatsman ->
//            val previousBatsman = previousInnings?.battingPerformances?.find { it.playerId == currentBatsman.playerId }
//            currentBatsman.sixes > (previousBatsman?.sixes ?: 0)
//        } ?: false
        return false
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
                targetSixes = batsmanWithNewSix.sixes + 2 // Target is current sixes + 2
            )
        )
    }
}

class SixesByPlayerQuestionGenerator(
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<SixesByPlayerParameter>,
    override val validator: SixesByPlayerQuestionValidator
) : BaseQuestionGenerator<SixesByPlayerParameter>(questionRepository) {
    override val type = QuestionType.SIX_BY_PLAYER

    override fun questionExists(param: SixesByPlayerParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetBatsmanIdAndTargetSixes(
            state.liveScorecard.matchId,
            QuestionType.SIX_BY_PLAYER,
            param.batsmanId.toInt(),
            param.targetSixes
        )
    }

    override fun createQuestion(
        param: SixesByPlayerParameter, state: MatchState
    ): Question? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null
        val batsman = currentInnings.battingPerformances.find { it.playerId == param.batsmanId.toInt() } ?: return null
        return createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${batsman.playerName} hit ${param.targetSixes} or more sixes in this innings?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
            category = listOf("Batting"),
            questionType = QuestionType.SIX_BY_PLAYER,
            targetBatsmanId = param.batsmanId.toInt(),
            targetSixes = param.targetSixes,
            param = param,
            state = state
        )
    }

    val INITIAL_WAGER = Pair(BigDecimal(5), BigDecimal(5))
     override fun calculateInitialWagers(param: SixesByPlayerParameter, state: MatchState): Pair<BigDecimal, BigDecimal> {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return INITIAL_WAGER
        val batsman =
            currentInnings.battingPerformances.find { it.playerId == param.batsmanId.toInt() } ?: return INITIAL_WAGER

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

        val wagerA = BigDecimal(10 * probability)
        val wagerB = BigDecimal(10).minus(wagerA)

        return Pair(wagerA, wagerB)
    }
}

class SixesByPlayerQuestionValidator : QuestionValidator {
    override fun validateParameters(matchState: MatchState): Boolean {
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        return currentInnings != null && currentInnings.battingPerformances.any { it.sixes > 0 }
    }

    override fun validateQuestion(question: Question): Boolean {
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
    override fun canResolve(question: Question, matchState: MatchState): Boolean {
        val targetBatsmanId = question.targetBatsmanId ?: return false
        val targetSixes = question.targetSixes ?: return false

        // Find the relevant innings (current or completed) where this batsman batted
        val relevantInnings = matchState.liveScorecard.innings.find { innings ->
            innings.battingPerformances.any { it.playerId == targetBatsmanId }
        }

        // Check if the batsman's performance can be found
        val batsmanPerformance = relevantInnings?.battingPerformances?.find { it.playerId == targetBatsmanId }

        return batsmanPerformance != null && (
                batsmanPerformance.sixes >= targetSixes ||  // Target achieved
                        batsmanPerformance.outDescription != null ||  // Batsman is out
                        !relevantInnings.isCurrentInnings ||  // Innings has ended
                        matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE  // Match has ended
                )
    }

    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val targetBatsmanId = question.targetBatsmanId ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetSixes = question.targetSixes ?: return QuestionResolution(false, PulseResult.UNDECIDED)

        // Find the relevant innings where this batsman batted
        val relevantInnings = matchState.liveScorecard.innings.find { innings ->
            innings.battingPerformances.any { it.playerId == targetBatsmanId }
        }
        val batsmanPerformance = relevantInnings?.battingPerformances?.find { it.playerId == targetBatsmanId }

        if (batsmanPerformance == null) {
            return QuestionResolution(false, PulseResult.UNDECIDED)
        }

        val sixesHit = batsmanPerformance.sixes
        val result = if (sixesHit >= targetSixes) PulseResult.Yes else PulseResult.No

        // Always resolve if the batsman is out, innings has ended, or match has ended
        return QuestionResolution(true, result)
    }
}



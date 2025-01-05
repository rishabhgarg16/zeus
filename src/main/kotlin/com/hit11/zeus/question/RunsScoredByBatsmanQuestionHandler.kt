package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.livedata.BattingPerformance
import com.hit11.zeus.livedata.Innings
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository
import java.math.BigDecimal

data class RunsScoredByBatsmanParameter(
    val targetBatsmanId: Int,
    val targetRuns: Int
) : QuestionParameter()

class RunsScoredByBatsmanTriggerCondition : TriggerCondition {

    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }

        return currentInnings?.battingPerformances?.any { currentBatsman ->
            val previousBatsman = previousInnings?.battingPerformances?.find { it.playerId == currentBatsman.playerId }
            val runsDifference = currentBatsman.runs - (previousBatsman?.runs ?: 0)
            runsDifference > 0 && (currentBatsman.runs % 25 == 0 || runsDifference >= 10)
        } ?: false
    }
}

class RunsScoredByBatsmanParameterGenerator : QuestionParameterGenerator<RunsScoredByBatsmanParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<RunsScoredByBatsmanParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings } ?: return emptyList()
        val previousInnings = currentState.liveScorecard.innings.find { !it.isCurrentInnings }

        return currentInnings.battingPerformances.mapNotNull { currentBatsman ->
            val previousBatsmanState = previousState?.liveScorecard?.innings
                ?.find { it.isCurrentInnings }
                ?.battingPerformances
                ?.find { it.playerId == currentBatsman.playerId }

            val runsDifference = currentBatsman.runs - (previousBatsmanState?.runs ?: 0)

            if (runsDifference > 0 && (currentBatsman.runs % 20 == 0 || runsDifference >= 10)) {
                val maxPossibleRuns = calculateMaxPossibleRuns(currentInnings, previousInnings, currentBatsman)
                val targetRuns = minOf(currentBatsman.runs + 20, maxPossibleRuns)

                if (targetRuns > currentBatsman.runs) {
                    RunsScoredByBatsmanParameter(currentBatsman.playerId, targetRuns)
                } else null
            } else null
        }
    }

    private fun calculateMaxPossibleRuns(
        currentInnings: Innings,
        previousInnings: Innings?,
        batsman: BattingPerformance
    ): Int {
        val totalInningsRuns = currentInnings.totalRuns
        val batsmanCurrentRuns = batsman.runs

        // If there's a previous innings, use its total as the target score
        val targetScore = previousInnings?.let { it.totalRuns + 1 } ?: Int.MAX_VALUE

        val remainingTeamRuns = targetScore - totalInningsRuns
        val maxPossibleIndividualRuns = batsmanCurrentRuns + remainingTeamRuns

        // Consider a realistic upper limit, say 250 runs for a single batsman in any format
        return minOf(maxPossibleIndividualRuns, 250)
    }
}

class RunsScoredByBatsmanQuestionGenerator(
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<RunsScoredByBatsmanParameter>,
    override val validator: RunsScoredByBatsmanQuestionValidator
) : BaseQuestionGenerator<RunsScoredByBatsmanParameter>(questionRepository) {
    override val type = QuestionType.RUNS_SCORED_BY_BATSMAN

    override fun questionExists(param: RunsScoredByBatsmanParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetBatsmanIdAndTargetRuns(
            state.liveScorecard.matchId,
            QuestionType.RUNS_SCORED_BY_BATSMAN,
            param.targetBatsmanId,
            param.targetRuns
        )
    }

    override fun createQuestion(param: RunsScoredByBatsmanParameter, state: MatchState): Question? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null
        val batsman = currentInnings.battingPerformances.find { it.playerId == param.targetBatsmanId } ?: return null

        val (wagerA, wagerB) = calculateInitialWagers(param, state)

        return createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${batsman.playerName} score ${param.targetRuns} or more runs in this innings?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
            category = listOf("Batting"),
            questionType = QuestionType.RUNS_SCORED_BY_BATSMAN,
            targetBatsmanId = param.targetBatsmanId,
            targetRuns = param.targetRuns,
            param = param,
            state = state
        )
    }

    val INITIAL_WAGER = Pair(BigDecimal(5), BigDecimal(5))
    override fun calculateInitialWagers(param: RunsScoredByBatsmanParameter, state: MatchState): Pair<BigDecimal, BigDecimal> {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return INITIAL_WAGER
        val batsman = currentInnings.battingPerformances.find { it.playerId == param.targetBatsmanId } ?: return INITIAL_WAGER

        val currentRuns = batsman.runs
        val targetRuns = param.targetRuns
        val remainingRuns = targetRuns - currentRuns

        val probability = when {
            remainingRuns <= 0 -> 0.9
            remainingRuns < 10 -> 0.8
            remainingRuns < 20 -> 0.7
            remainingRuns < 30 -> 0.6
            remainingRuns < 40 -> 0.5
            else -> 0.4
        }

        val wagerA = BigDecimal(10 * probability)
        val wagerB = BigDecimal(10).minus(wagerA)

        return Pair(wagerA, wagerB)
    }
}

class RunsScoredByBatsmanQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: Question): Boolean {
        if (question.questionType != QuestionType.RUNS_SCORED_BY_BATSMAN) {
            return false
        }
        if (question.targetBatsmanId == null) {
            throw QuestionValidationException(
                "Target batsman ID is required for Runs Scored by Batsman question ${question.id}"
            )
        }
        if (question.targetRuns == null || question.targetRuns <= 0) {
            throw QuestionValidationException(
                "Target runs must be a positive number for Runs Scored by Batsman question ${question.id}"
            )
        }
        return true
    }

    override fun validateParameters(matchState: MatchState): Boolean {
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        return currentInnings != null && currentInnings.battingPerformances.any { it.runs > 0 }
    }
}

class RunsScoredByBatsmanResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: Question, matchState: MatchState): Boolean {
        val targetBatsmanId = question.targetBatsmanId ?: return false
        val targetRuns = question.targetRuns ?: return false

        // Find the relevant innings (current or completed) where this batsman batted
        val relevantInnings = matchState.liveScorecard.innings.find { innings ->
            innings.battingPerformances.any { it.playerId == targetBatsmanId }
        }

        // Check if the batsman's performance can be found
        val batsmanPerformance = relevantInnings?.battingPerformances?.find { it.playerId == targetBatsmanId }

        return batsmanPerformance != null && (
                batsmanPerformance.runs >= targetRuns ||  // Target achieved
                        batsmanPerformance.dismissed ||  // Batsman is out
                        !relevantInnings.isCurrentInnings ||  // Innings has ended
                        matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE  // Match has ended
                )
    }

    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val targetBatsmanId = question.targetBatsmanId ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetRuns = question.targetRuns ?: return QuestionResolution(false, PulseResult.UNDECIDED)

        // Find the relevant innings where this batsman batted
        val relevantInnings = matchState.liveScorecard.innings.find { innings ->
            innings.battingPerformances.any { it.playerId == targetBatsmanId }
        }
        val batsmanPerformance = relevantInnings?.battingPerformances?.find { it.playerId == targetBatsmanId }

        if (batsmanPerformance == null) {
            return QuestionResolution(false, PulseResult.UNDECIDED)
        }

        val runsScored = batsmanPerformance.runs
        val result = if (runsScored >= targetRuns) PulseResult.Yes else PulseResult.No

        // Resolve if the target is met, batsman is dismissed, innings has ended, or match has ended
        val canResolve = runsScored >= targetRuns ||
                batsmanPerformance.dismissed ||
                !relevantInnings.isCurrentInnings ||
                matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE

        return QuestionResolution(canResolve, if (canResolve) result else PulseResult.UNDECIDED)
    }
}
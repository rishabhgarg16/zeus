package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.CricbuzzMatchPlayingState
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionType

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

class RunsScoredByBatsmanParameterGenerator
    : QuestionParameterGenerator<RunsScoredByBatsmanParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<RunsScoredByBatsmanParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings } ?: return emptyList()
        val previousInnings = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }

        return currentInnings.battingPerformances.mapNotNull { currentBatsman ->
            val previousBatsman = previousInnings?.battingPerformances?.find { it.playerId == currentBatsman.playerId }
            val runsDifference = currentBatsman.runs - (previousBatsman?.runs ?: 0)

            if (runsDifference > 0 && (currentBatsman.runs % 25 == 0 || runsDifference >= 10)) {
                RunsScoredByBatsmanParameter(currentBatsman.playerId, currentBatsman.runs + 25)
            } else null
        }
    }
}

class RunsScoredByBatsmanQuestionGenerator(
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<RunsScoredByBatsmanParameter>,
    override val validator: RunsScoredByBatsmanQuestionValidator
) : BaseQuestionGenerator<RunsScoredByBatsmanParameter>() {
    override val type = QuestionType.RUNS_SCORED_BY_BATSMAN

    override fun createQuestion(param: RunsScoredByBatsmanParameter, state: MatchState): QuestionDataModel? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null
        val batsman = currentInnings.battingPerformances.find { it.playerId == param.targetBatsmanId } ?: return null

        val (wagerA, wagerB) = calculateInitialWagers(param, state)

        return createDefaultQuestionDataModel(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${batsman.playerName} score ${param.targetRuns} or more runs in this innings?",
            optionA = "Yes",
            optionB = "No",
            category = listOf("Batting"),
            questionType = QuestionType.RUNS_SCORED_BY_BATSMAN,
            targetBatsmanId = param.targetBatsmanId,
            targetRuns = param.targetRuns,
            param = param,
            state = state
        )
    }

    override fun calculateInitialWagers(param: RunsScoredByBatsmanParameter, state: MatchState): Pair<Long, Long> {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return Pair(5L, 5L)
        val batsman = currentInnings.battingPerformances.find { it.playerId == param.targetBatsmanId } ?: return Pair(5L, 5L)

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

        val wagerA = (10 * probability).toLong().coerceIn(1L, 9L)
        val wagerB = 10 - wagerA

        return Pair(wagerA, wagerB)
    }
}

class RunsScoredByBatsmanQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: QuestionDataModel): Boolean {
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
    override fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean {
        val targetBatsmanId = question.targetBatsmanId ?: return false
        val targetRuns = question.targetRuns ?: return false
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val batsmanPerformance = currentInnings?.battingPerformances?.find { it.playerId == targetBatsmanId }

        return batsmanPerformance != null &&
                (batsmanPerformance.runs >= targetRuns ||
                        batsmanPerformance.outDescription != null ||
                        matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE)
    }

    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetBatsmanId = question.targetBatsmanId ?: return QuestionResolution(false, null)
        val targetRuns = question.targetRuns ?: return QuestionResolution(false, null)
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val batsmanPerformance = currentInnings?.battingPerformances?.find { it.playerId == targetBatsmanId }

        val isResolved = batsmanPerformance?.runs?.let { it >= targetRuns } ?: false
        val result = if (isResolved) "Yes" else "No"

        return QuestionResolution(true, result)
    }
}
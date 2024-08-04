package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.CricbuzzMatchPlayingState
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionType
import java.math.BigDecimal

data class TeamRunsInQuestionParameter(
    val targetRuns: Int,
    val targetOvers: Int
) : QuestionParameter()

class TeamRunsInMatchTrigger(private val triggerEveryNOvers: Int) : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val currentOver = currentState.liveScorecard.innings.find { it.isCurrentInnings }?.overs?.toInt() ?: 0
        val previousOver = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }?.overs?.toInt() ?: 0
        return currentOver % triggerEveryNOvers == 0 && currentOver != previousOver
    }
}

class TeamRunsInMatchParameterGenerator : QuestionParameterGenerator<TeamRunsInQuestionParameter> {
    override fun generateParameters(
        currentState: MatchState,
        previousState: MatchState?
    ): List<TeamRunsInQuestionParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        val currentRuns = currentInnings?.totalRuns ?: 0
        val currentOver = currentInnings?.overs?.toInt() ?: 0

        val baseTarget = ((currentRuns / 10) + 1) * 10
        val targetOvers = (currentOver + 5).coerceAtMost(20)

        return listOf(
            TeamRunsInQuestionParameter(targetRuns = baseTarget, targetOvers = targetOvers),
            TeamRunsInQuestionParameter(targetRuns = baseTarget + 10, targetOvers = targetOvers),
            TeamRunsInQuestionParameter(targetRuns = baseTarget + 20, targetOvers = targetOvers)
        )
    }
}


class TeamRunsInMatchQuestionGenerator(
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<TeamRunsInQuestionParameter>,
    override val validator: TeamRunsInMatchQuestionValidator
) : BaseQuestionGenerator<TeamRunsInQuestionParameter>() {
    override val type = QuestionType.TEAM_RUNS_IN_MATCH

    override fun generateQuestions(currentState: MatchState, previousState: MatchState?): List<QuestionDataModel> {
        if (!triggerCondition.shouldTrigger(currentState, previousState)) {
            return emptyList()
        }
        val parameters = parameterGenerator.generateParameters(currentState, previousState)
        return parameters.mapNotNull { param ->
            createQuestion(param, currentState)?.takeIf { validator.validateQuestion(it) }
        }
    }

    override fun createQuestion(
        param: TeamRunsInQuestionParameter,
        state: MatchState
    ): QuestionDataModel? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null

        return createDefaultQuestionDataModel(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${currentInnings.battingTeam!!.name} score ${param.targetRuns} or more runs by the ${param.targetOvers}th over?",
            optionA = "Yes",
            optionB = "No",
            category = listOf("Batting"),
            questionType = QuestionType.TEAM_RUNS_IN_MATCH,
            targetTeamId = currentInnings.battingTeam.id,
            targetRuns = param.targetRuns,
            targetOvers = param.targetOvers,
            param = param,
            state = state
        )
    }

    override fun calculateInitialWagers(param: TeamRunsInQuestionParameter, state: MatchState): Pair<Long, Long> {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return Pair(5L, 5L)
        val currentRunRate = currentInnings.runRate
        val targetRunRate = param.targetRuns.toFloat() / param.targetOvers

        val probability = when {
            targetRunRate > currentRunRate * 1.3 -> 0.3
            targetRunRate > currentRunRate * 1.1 -> 0.5
            targetRunRate > currentRunRate -> 0.7
            else -> 0.8
        }

        val wagerA = (10 * probability).toLong().coerceIn(1L, 9L)
        val wagerB = 10 - wagerA

        return Pair(wagerA, wagerB)
    }
}

class TeamRunsInMatchQuestionValidator : QuestionValidator {
    override fun validateParameters(matchState: MatchState): Boolean {
        // Implement validation logic
        // For example, check if the match is in a valid state for this question type
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        return currentInnings != null && currentInnings.overs.toInt() < 20
    }

    override fun validateQuestion(question: QuestionDataModel): Boolean {
        if (question.questionType != QuestionType.TEAM_RUNS_IN_MATCH) {
            return false
        }

        if (question.targetTeamId == null) {
            throw QuestionValidationException(
                "Target team ID is required for Team Runs in Match question ${question.id}"
            )
        }
        if (question.targetRuns == null || question.targetRuns <= 0) {
            throw QuestionValidationException(
                "Target runs must be a positive number for Team Runs question ${question.id}"
            )
        }
        if (question.targetOvers == null) {
            throw QuestionValidationException("Target overs is required for Team Runs in Match question ${question.id}")
        }

        if (question.optionA.isBlank() || question.optionB.isNotBlank()) {
            throw QuestionValidationException("Options are blank for question ${question.id}")
        }
        return true
    }
}


class TeamRunsInMatchResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: QuestionDataModel, matchState: MatchState): Boolean {
        val targetTeamId = question.targetTeamId ?: return false
        val targetRuns = question.targetRuns ?: return false
        val targetInnings = matchState.liveScorecard.innings.find { it.battingTeam?.id == targetTeamId }

        return targetInnings != null &&
                (targetInnings.overs.toInt()) >= (question.targetOvers ?: 20) &&
                (targetInnings.totalRuns >= targetRuns || matchState.liveScorecard.state ==
                        CricbuzzMatchPlayingState.COMPLETE)
    }

    override fun resolve(question: QuestionDataModel, matchState: MatchState): QuestionResolution {

        val targetRuns = question.targetRuns ?: return QuestionResolution(false, null)
        val targetOvers = question.targetOvers ?: return QuestionResolution(false, null)
        val targetBalls = targetOvers * 6

        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val currentRuns = currentInnings?.totalRuns ?: -1

        val currentBall = currentInnings?.overs?.multiply(BigDecimal(10))?.remainder(BigDecimal(10))?.toInt() ?: 0
        val currentOver = currentInnings?.overs?.toInt() ?: 0
        val ballNumber = currentOver * 6 + currentBall

        // we should skip updating this question
        if (ballNumber < targetBalls && currentRuns < targetRuns) {
            return QuestionResolution(false, null)
        }

        // state has crossed so update the question and not result/payouts
        if (ballNumber > targetBalls) {
            return QuestionResolution(true, null)
        }

        // check if innings over or chasing in 2nd innings
        val result = if (currentRuns >= targetRuns) "Yes" else "No"
        return QuestionResolution(true, result)
    }
}
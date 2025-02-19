package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository
import java.math.BigDecimal

data class TeamRunsInQuestionParameter(
    val targetTeamId: Int, val targetRuns: Int, val targetOvers: Int
) : QuestionParameter()

class TeamRunsInMatchTrigger(private val triggerEveryNOvers: Int) : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val currentOver = currentState.liveScorecard.innings.find { it.isCurrentInnings }?.overs?.toInt() ?: 0
        val previousOver = previousState?.liveScorecard?.innings?.find { it.isCurrentInnings }?.overs?.toInt() ?: 0
        val isActiveState = when (currentState.liveScorecard.state) {
            CricbuzzMatchPlayingState.IN_PROGRESS,
            CricbuzzMatchPlayingState.INNINGS_BREAK,
            CricbuzzMatchPlayingState.TEA,
            CricbuzzMatchPlayingState.DRINK,
            CricbuzzMatchPlayingState.TOSS,
            CricbuzzMatchPlayingState.STUMPS -> true
            else -> false
        }
        return currentOver != previousOver && isActiveState
    }
}

class TeamRunsInMatchParameterGenerator : QuestionParameterGenerator<TeamRunsInQuestionParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<TeamRunsInQuestionParameter> {
        // These intervals represent how many overs ahead we want to set questions for
        val intervals = listOf(1, 3, 5)

        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        val currentRuns = currentInnings?.totalRuns ?: 0
        val currentOver = currentInnings?.overs?.toInt() ?: 0
        val currentRunRate = currentInnings?.runRate ?: 0F
        val targetTeamId = currentInnings?.battingTeam?.id ?: 0
        val maxOvers = when (currentState.liveScorecard.matchFormat) {
            MatchFormat.T20 -> 20
            MatchFormat.ODI -> 50
            MatchFormat.TEST -> 90
        }
        return intervals.map { interval ->
            // Calculate target overs for this interval
            val targetOvers = (currentOver + interval).coerceAtMost(maxOvers)
            // Project total runs by the target over (using current run rate)
            val projectedTotalRuns = (currentRunRate * targetOvers).toInt()
            // Use a rounded figure as a base target; ensure it's above the current score
            val baseTarget = (((projectedTotalRuns / 10) + 1) * 10).coerceAtLeast(currentRuns + 1)
            // Compute the run rate required for the remaining overs to hit the base target
            val remainingOvers = (targetOvers - currentOver).coerceAtLeast(1)
            val requiredRate = (baseTarget - currentRuns).toFloat() / remainingOvers

            // Adjust the target:
            //   - If required rate is more than 130% of current run rate, make target tougher (+10)
            //   - If it's below the current rate, relax the target (-10)
            val adjustedTarget = when {
                requiredRate > currentRunRate * 1.3 -> baseTarget + 10
                requiredRate < currentRunRate -> baseTarget - 10
                else -> baseTarget
            }.coerceAtLeast(currentRuns + 1)

            TeamRunsInQuestionParameter(
                targetRuns = adjustedTarget,
                targetOvers = targetOvers,
                targetTeamId = targetTeamId
            )
        }
    }
}


class TeamRunsInMatchQuestionGenerator(
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val parameterGenerator: QuestionParameterGenerator<TeamRunsInQuestionParameter>,
    override val validator: TeamRunsInMatchQuestionValidator
) : BaseQuestionGenerator<TeamRunsInQuestionParameter>(questionRepository) {
    override val type = QuestionType.TEAM_RUNS_IN_MATCH

    override fun questionExists(param: TeamRunsInQuestionParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetTeamIdAndTargetRunsAndTargetOvers(
            state.liveScorecard.matchId, QuestionType.TEAM_RUNS_IN_MATCH,
            param.targetTeamId,
            param.targetRuns,
            param.targetOvers
        )
    }

    override fun generateQuestions(currentState: MatchState, previousState: MatchState?): List<Question> {
        if (!triggerCondition.shouldTrigger(currentState, previousState)) {
            return emptyList()
        }

        // Deduplicate parameters in the same batch
        val parameters = parameterGenerator.generateParameters(currentState, previousState)
            .distinctBy { param ->
                Triple(param.targetTeamId, param.targetRuns, param.targetOvers)
            }

        return parameters.mapNotNull { param ->
            if (!questionExists(param, currentState)) {
                createQuestion(param, currentState)?.takeIf { validator.validateQuestion(it) }
            } else {
                null
            }
        }
    }

    fun getOrdinal(n: Int) = when {
        n % 100 in 11..13 -> "${n}th"
        n % 10 == 1 -> "${n}st"
        n % 10 == 2 -> "${n}nd"
        n % 10 == 3 -> "${n}rd"
        else -> "${n}th"
    }

    override fun createQuestion(
        param: TeamRunsInQuestionParameter, state: MatchState
    ): Question? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null

        return createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${currentInnings.battingTeam!!.name} score ${param.targetRuns} OR OVER runs by the " +
                    "${getOrdinal(param.targetOvers)} over?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
            category = listOf("Batting"),
            questionType = QuestionType.TEAM_RUNS_IN_MATCH,
            targetTeamId = currentInnings.battingTeam.id,
            targetRuns = param.targetRuns,
            targetOvers = param.targetOvers,
            param = param,
            state = state
        )
    }

    override fun calculateInitialWagers(
        param: TeamRunsInQuestionParameter, state: MatchState
    ): Pair<BigDecimal, BigDecimal> {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return INITIAL_WAGER
        val currentRunRate = currentInnings.runRate
        val targetRunRate = param.targetRuns.toFloat() / param.targetOvers

        // Simple odds modelling: probability is inversely proportional to how much higher the target run rate is.
        val diffRatio = (targetRunRate - currentRunRate) / currentRunRate
        // Here we set a base probability adjusted by diffRatio, clamped between 0.2 and 0.8
        val probability = (1 - diffRatio).coerceIn(0.2f, 0.8f)

        val wagerA = BigDecimal(10).multiply(probability.toBigDecimal())
        val wagerB = BigDecimal(10).subtract(wagerA)

        return Pair(wagerA, wagerB)
    }
}

class TeamRunsInMatchQuestionValidator : QuestionValidator {
    override fun validateParameters(matchState: MatchState): Boolean {
        // Implement validation logic
        // For example, check if the match is in a valid state for this question type
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val maxOvers = when (matchState.liveScorecard.matchFormat) {
            MatchFormat.TEST -> 90
            MatchFormat.ODI -> 50
            MatchFormat.T20 -> 20
            else -> 20
        }
        return currentInnings != null && currentInnings.overs.toInt() < maxOvers
    }

    override fun validateQuestion(question: Question): Boolean {
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

        if (question.optionA.isBlank() || question.optionB.isBlank()) {
            throw QuestionValidationException(
                "Options are blank for question ${question.id} and question type " +
                        "${question.questionType}"
            )
        }
        return true
    }
}


class TeamRunsInMatchResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: Question, matchState: MatchState): Boolean {
        val targetTeamId = question.targetTeamId ?: return false
        val targetRuns = question.targetRuns ?: return false
        val targetOvers = question.targetOvers ?: return false
        val targetBalls = targetOvers * 6

        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
            ?: return false

        // Verify this is the right team batting
        if (currentInnings.battingTeam?.id != targetTeamId) {
            return false
        }

        val currentOvers = currentInnings.totalBalls() / 6.0  // Convert to decimal overs
        if (currentOvers > targetOvers) {
            return true
        }

        return when {
            // All out
            currentInnings.wickets >= 10 -> true

            currentInnings.totalBalls() >= (targetOvers * 6) -> true

            // Innings has ended before target overs (all out or declaration)
            matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE -> true

            // Target runs achieved before target overs
            currentInnings.totalRuns >= targetRuns -> true

            // Otherwise, the question isn't ready to be resolved
            else -> false
        }
    }


    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val targetTeamId = question.targetTeamId ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetRuns = question.targetRuns ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetOvers = question.targetOvers ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetBalls = targetOvers * 6
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
            ?: return QuestionResolution(false, PulseResult.UNDECIDED)

        if (currentInnings.battingTeam?.id != targetTeamId) {
            return QuestionResolution(false, PulseResult.UNDECIDED)
        }

        val currentRuns = currentInnings.totalRuns
        val currentBalls = currentInnings.totalBalls()

        // Condition 1: Target overs have been bowled
        if (currentBalls >= targetBalls) {
            val result = if (currentRuns >= targetRuns) PulseResult.Yes else PulseResult.No
            return QuestionResolution(true, result)
        }

        // All out
        if(currentInnings.wickets >= 10) {
            val result = if (currentRuns >= targetRuns) PulseResult.Yes else PulseResult.No
            return QuestionResolution(true, result)
        }

        // Condition 2: Innings has ended before target overs (all out or declaration)
        if (!currentInnings.isCurrentInnings && currentBalls < targetBalls) {
            val result = if (currentRuns >= targetRuns) PulseResult.Yes else PulseResult.No
            return QuestionResolution(true, result)
        }

        // Condition 3: Match has ended
        if (matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE) {
            val result = if (currentRuns >= targetRuns) PulseResult.Yes else PulseResult.No
            return QuestionResolution(true, result)
        }

        // Condition 4: Target runs achieved before target overs
        if (currentRuns >= targetRuns) {
            return QuestionResolution(true, PulseResult.Yes)
        }

        // Otherwise, resolution isn't possible yet
        return QuestionResolution(false, PulseResult.UNDECIDED)
    }
}
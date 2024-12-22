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
        return currentOver != previousOver && (
                currentState.liveScorecard.state == CricbuzzMatchPlayingState.IN_PROGRESS ||
                        currentState.liveScorecard.state == CricbuzzMatchPlayingState.PREVIEW)
    }
}

class TeamRunsInMatchParameterGenerator : QuestionParameterGenerator<TeamRunsInQuestionParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<TeamRunsInQuestionParameter> {
        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        val currentRuns = currentInnings?.totalRuns ?: 0
        val currentOver = currentInnings?.overs?.toInt() ?: 0

        val baseTarget = ((currentRuns / 10) + 1) * 10
        val maxOvers = if (currentState.liveScorecard.matchFormat == MatchFormat.ODI) 50 else 20

        val targetTeamId = currentInnings?.battingTeam?.id ?: 0

        return listOf(
            TeamRunsInQuestionParameter(
                targetRuns = baseTarget,
                targetOvers = (currentOver + 1).coerceAtMost(maxOvers),
                targetTeamId = targetTeamId
            ),
            TeamRunsInQuestionParameter(
                targetRuns = baseTarget + 10,
                targetOvers = (currentOver + 1).coerceAtMost(maxOvers),
                targetTeamId = targetTeamId
            ),
            TeamRunsInQuestionParameter(
                targetRuns = baseTarget + 20,
                targetOvers = (currentOver + 2).coerceAtMost(maxOvers),
                targetTeamId = targetTeamId
            )
        )
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
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetTeamIdAndTargetRuns(
            state.liveScorecard.matchId, QuestionType.TEAM_RUNS_IN_MATCH,
            param.targetTeamId,
            param.targetRuns
        )
    }

    override fun generateQuestions(currentState: MatchState, previousState: MatchState?): List<Question> {
        if (!triggerCondition.shouldTrigger(currentState, previousState)) {
            return emptyList()
        }
        val parameters = parameterGenerator.generateParameters(currentState, previousState)
        return parameters.mapNotNull { param ->
            createQuestion(param, currentState)?.takeIf { validator.validateQuestion(it) }
        }
    }

    override fun createQuestion(
        param: TeamRunsInQuestionParameter, state: MatchState
    ): Question? {
        val currentInnings = state.liveScorecard.innings.find { it.isCurrentInnings } ?: return null

        return createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${currentInnings.battingTeam!!.name} score ${param.targetRuns} or more runs by the ${param.targetOvers}th over?",
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

        val targetInnings = matchState.liveScorecard.innings.find { it.battingTeam?.id == targetTeamId }
            ?: return false

        val currentRuns = targetInnings.totalRuns
        val currentOvers = targetInnings.overs
        val currentBalls =
            (currentOvers.toInt() * 6) + (currentOvers.remainder(BigDecimal.ONE).multiply(BigDecimal(10))).toInt()

        return when {
            // Target overs have been bowled
            currentBalls >= targetBalls -> true

            // Innings has ended before target overs (all out or declaration)
            !targetInnings.isCurrentInnings && currentBalls < targetBalls -> true

            // Match has ended
            matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE -> true

            // Target runs achieved before target overs
            currentRuns >= targetRuns -> true

            // Otherwise, can't resolve yet
            else -> false
        }
    }


    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val targetTeamId = question.targetTeamId ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetRuns = question.targetRuns ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetOvers = question.targetOvers ?: return QuestionResolution(false, PulseResult.UNDECIDED)
        val targetBalls = targetOvers * 6

        val targetInnings = matchState.liveScorecard.innings.find { it.battingTeam?.id == targetTeamId }
            ?: return QuestionResolution(false, PulseResult.UNDECIDED)

        val currentRuns = targetInnings.totalRuns
        val currentOvers = targetInnings.overs
        val currentBalls =
            (currentOvers.toInt() * 6) + (currentOvers.remainder(BigDecimal.ONE).multiply(BigDecimal(10))).toInt()

        // Case 1: Target overs have been bowled
        if (currentBalls >= targetBalls) {
            val result = if (currentRuns >= targetRuns) PulseResult.Yes else PulseResult.No
            return QuestionResolution(true, result)
        }

        // Case 2: Innings has ended before target overs (all out or declaration)
        if (!targetInnings.isCurrentInnings && currentBalls < targetBalls) {
            val result = if (currentRuns >= targetRuns) PulseResult.Yes else PulseResult.No
            return QuestionResolution(true, result)
        }

        // Case 3: Match has ended
        if (matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE) {
            val result = if (currentRuns >= targetRuns) PulseResult.Yes else PulseResult.No
            return QuestionResolution(true, result)
        }

        // Case 4: Target runs achieved before target overs
        if (currentRuns >= targetRuns) {
            return QuestionResolution(true, PulseResult.Yes)
        }

        // If none of the above conditions are met, the question can't be resolved yet
        return QuestionResolution(false, PulseResult.UNDECIDED)
    }
}
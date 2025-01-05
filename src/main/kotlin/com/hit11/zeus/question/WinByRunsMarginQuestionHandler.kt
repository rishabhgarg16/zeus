package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository
import java.math.BigDecimal

data class WinByRunsMarginParameter(
    val targetTeamId: Int,
    val targetMargin: Int
) : QuestionParameter()

class WinByRunsMarginTriggerCondition : TriggerCondition {
    override fun shouldTrigger(currentState: MatchState, previousState: MatchState?): Boolean {
        val innings = currentState.liveScorecard.innings
        val previousInnings = previousState?.liveScorecard?.innings

        // Trigger when the second innings starts and every 5 overs during the second innings
        return when {
            innings.size == 2 && previousInnings?.size == 1 -> true
            innings.size == 2 &&
                    innings[1].overs.toInt() % 5 == 0 &&
                    innings[1].overs != previousInnings?.get(1)?.overs -> true

            else -> false
        }
    }
}

class WinByRunsMarginParameterGenerator : QuestionParameterGenerator<WinByRunsMarginParameter> {
    override fun generateParameters(
        currentState: MatchState, previousState: MatchState?
    ): List<WinByRunsMarginParameter> {
        if (currentState.liveScorecard.innings.size < 2) return emptyList()

        val currentInnings = currentState.liveScorecard.innings.find { it.isCurrentInnings }
        if (currentInnings == null) return emptyList()

        val previousInningsId = if (currentInnings.inningsId == 1) 0 else 1
        val previousInnings = currentState.liveScorecard.innings[previousInningsId]

        val firstInningsScore = previousInnings.totalRuns
        val secondInningsScore = currentInnings.totalRuns
        val remainingRuns = firstInningsScore - (secondInningsScore ?: 0)
        val remainingOvers = (currentInnings.overs.toInt() + 1) - 20

        val margins = when {
            remainingOvers > 10 -> listOf(remainingRuns / 4, remainingRuns / 2, remainingRuns * 3 / 4)
            remainingOvers > 5 -> listOf(remainingRuns / 3, remainingRuns / 2, remainingRuns * 2 / 3)
            else -> listOf(remainingRuns / 4, remainingRuns / 3, remainingRuns / 2)
        }.map { it.coerceAtLeast(1) } // Ensure margin is at least 1 run

        return listOf(
            WinByRunsMarginParameter(currentInnings.battingTeam!!.id, margins[0]),
            WinByRunsMarginParameter(currentInnings.battingTeam.id, margins[1]),
            WinByRunsMarginParameter(currentInnings.battingTeam.id, margins[2])
        )
    }
}

class WinByRunsMarginQuestionGenerator(
    override val parameterGenerator: QuestionParameterGenerator<WinByRunsMarginParameter>,
    questionRepository: QuestionRepository,
    override val triggerCondition: TriggerCondition,
    override val validator: WinByRunsMarginQuestionValidator
) : BaseQuestionGenerator<WinByRunsMarginParameter>(questionRepository) {
    override val type = QuestionType.WIN_BY_RUNS_MARGIN

    override fun questionExists(param: WinByRunsMarginParameter, state: MatchState): Boolean {
        return questionRepository.existsByMatchIdAndQuestionTypeAndTargetTeamIdAndTargetRuns(
            state.liveScorecard.matchId,
            QuestionType.WIN_BY_RUNS_MARGIN,
            param.targetTeamId,
            param.targetMargin
        )
    }

    override fun createQuestion(param: WinByRunsMarginParameter, state: MatchState): Question? {
        val battingTeam = state.liveScorecard.innings.find { it.isCurrentInnings }?.battingTeam
        val team = if (battingTeam?.id == param.targetTeamId) battingTeam else return null
        return createDefaultQuestion(
            matchId = state.liveScorecard.matchId,
            pulseQuestion = "Will ${team.name} win by ${param.targetMargin} or more runs?",
            optionA = PulseOption.Yes.name,
            optionB = PulseOption.No.name,
            category = listOf("Match Result"),
            questionType = QuestionType.WIN_BY_RUNS_MARGIN,
            targetTeamId = param.targetTeamId,
            targetRuns = param.targetMargin,
            param = param,
            state = state
        )
    }

    override fun calculateInitialWagers(param: WinByRunsMarginParameter, state: MatchState): Pair<BigDecimal, BigDecimal> {
        if (state.liveScorecard.innings.size < 2) return INITIAL_WAGER

        val secondInnings = state.liveScorecard.innings.find { it.isCurrentInnings }
        if (secondInnings == null) return INITIAL_WAGER

        val previousInningsId = if (secondInnings.inningsId == 1) 0 else 1
        val firstInnings = state.liveScorecard.innings[previousInningsId]


        val firstInningsScore = firstInnings.totalRuns
        val secondInningsScore = secondInnings.totalRuns
        val remainingRuns = firstInningsScore - secondInningsScore
        val matchFormat = state.liveScorecard.matchFormat
        // TODO check total match over from the match
        val remainingOvers = (if (matchFormat == MatchFormat.ODI) 50 else 20) - (secondInnings.overs.toInt() + 1)

        val probability = when {
            remainingRuns <= 0 -> 0.9
            remainingOvers == 0 -> 0.1
            else -> {
                val requiredRunRate = remainingRuns.toFloat() / remainingOvers
                val currentRunRate = secondInnings.runRate
                when {
                    requiredRunRate > currentRunRate * 1.5 -> 0.2
                    requiredRunRate > currentRunRate * 1.2 -> 0.4
                    requiredRunRate > currentRunRate -> 0.6
                    else -> 0.8
                }
            }
        }

        val wagerA = BigDecimal(10 * probability)
        val wagerB = BigDecimal(10).minus(wagerA)

        return Pair(wagerA, wagerB)
    }
}

class WinByRunsMarginQuestionValidator : QuestionValidator {
    override fun validateQuestion(question: Question): Boolean {
        if (question.questionType != QuestionType.WIN_BY_RUNS_MARGIN) {
            return false
        }
        if (question.targetTeamId == null || question.targetTeamId == 0) {
            throw QuestionValidationException("Target team must be specified for Win by Runs Margin question")
        }
        if (question.targetRuns == null || question.targetRuns <= 0) {
            throw QuestionValidationException("Target margin must be a positive number for Win by Runs Margin question")
        }
        return true
    }

    override fun validateParameters(matchState: MatchState): Boolean {
        return matchState.liveScorecard.innings.size == 2 &&
                matchState.liveScorecard.state == CricbuzzMatchPlayingState.IN_PROGRESS
    }
}

class WinByRunsMarginResolutionStrategy : ResolutionStrategy {
    override fun canResolve(question: Question, matchState: MatchState): Boolean =
        matchState.liveScorecard.state == CricbuzzMatchPlayingState.COMPLETE

    override fun resolve(question: Question, matchState: MatchState): QuestionResolution {
        val result = matchState.liveScorecard.result
        val isCorrectTeam = result?.winningTeamId == question.targetTeamId
        val isCorrectMargin = result?.winByRuns == true && (result.winningMargin >= (question.targetRuns ?: 0))
        return QuestionResolution(true, if (isCorrectTeam && isCorrectMargin) PulseResult.Yes else PulseResult.No)
    }
}
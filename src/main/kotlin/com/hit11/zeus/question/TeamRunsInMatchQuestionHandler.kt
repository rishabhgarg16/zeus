package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import java.math.BigDecimal

class TeamRunsInMatchQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetTeamId == null) {
            throw QuestionValidationException("Target team ID is required for Team Runs in Match question ${question.id}")
        }
        if (question.targetRuns == null || question.targetRuns <= 0) {
            throw QuestionValidationException("Target runs must be a positive number for Team Runs question ${question.id}")
        }
        if (question.targetOvers == null) {
            throw QuestionValidationException("Target overs is required for Team Runs in Match question ${question.id}")
        }
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        return currentInnings?.battingTeam?.id == question.targetTeamId
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (!canBeResolved(question, matchState)) {
            return QuestionResolution(false, null)
        }

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
        if(ballNumber >  targetBalls) {
            return QuestionResolution(true, null)
        }

        // check if innings over or chasing in 2nd innings
        val result = if (currentRuns >= targetRuns) "Yes" else "No"
        return QuestionResolution(true, result)
    }
}
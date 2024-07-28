package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class TeamRunsInMatchQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetTeamId == null) {
            throw QuestionValidationException("Target team ID is required for Team Runs in Match question")
        }
        if (question.targetRuns == null || question.targetRuns <= 0) {
            throw QuestionValidationException("Target runs must be a positive number for Team Runs in Match question")
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
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val currentRuns = currentInnings?.totalRuns ?: -1

        // check if innings over or chasing in 2nd innings
        val result = if (currentRuns >= targetRuns) "Yes" else "No"

        return QuestionResolution(true, result)
    }
}
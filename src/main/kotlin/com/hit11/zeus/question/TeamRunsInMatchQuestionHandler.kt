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
        return matchState.currentInning.battingTeamId == question.targetTeamId
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (!canBeResolved(question, matchState)) {
            return QuestionResolution(false, null)
        }

        val targetRuns = question.targetRuns ?: return QuestionResolution(false, null)
        val currentRuns = matchState.currentInning.totalRuns

        val isResolved = currentRuns >= targetRuns
        val result = if (isResolved) "Yes" else "No"

        return QuestionResolution(isResolved, result)
    }
}
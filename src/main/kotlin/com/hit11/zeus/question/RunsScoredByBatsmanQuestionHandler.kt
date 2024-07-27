package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class RunsScoredByBatsmanQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetBatsmanId == null) {
            throw QuestionValidationException("Target batsman ID is required for Century by Batsman question ${question.id}")
        }

        if (question.targetRuns == null) {
            throw QuestionValidationException("Target Runs is required for Century by Batsman question ${question.id}")
        }
    }
    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        return true // This can be resolved at any time during the match
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetBatsmanId = question.targetBatsmanId ?: return QuestionResolution(false, null)
        val targetRuns = question.targetRuns ?: return QuestionResolution(false, null)

        val batsmanPerformance = matchState.liveScorecard.innings.battingPerformances
            .find { it.playerId == targetBatsmanId }

        val isResolved = batsmanPerformance?.runs?.let { it >= targetRuns } ?: false
        val result = if (isResolved) "Yes" else "No"

        return QuestionResolution(isResolved, result)
    }
}
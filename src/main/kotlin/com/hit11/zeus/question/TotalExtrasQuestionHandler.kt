package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class TotalExtrasQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetExtras == null || question.targetExtras <= 0) {
            throw QuestionValidationException("Target extras must be a positive number for question ${question.id}")
        }
        if (question.targetTeamId == null) {
            throw QuestionValidationException("Target team ID is required for question ${question.id}")
        }
    }
    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        return matchState.liveScorecard.innings.bowlingTeam.id == question.targetTeamId
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (!canBeResolved(question, matchState)) {
            return QuestionResolution(false, null)
        }
        val targetExtras = question.targetExtras ?: return QuestionResolution(false, null)
        val currentExtras = matchState.liveScorecard.innings.totalExtras

        val isResolved = currentExtras >= targetExtras
        // check for end of innings
        val result = if (isResolved) "Yes" else "No"

        return QuestionResolution(isResolved, result)
    }
}
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
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        return currentInnings?.bowlingTeam?.id == question.targetTeamId
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (!canBeResolved(question, matchState)) {
            return QuestionResolution(false, null)
        }
        val targetExtras = question.targetExtras ?: return QuestionResolution(false, null)
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val currentExtras = currentInnings?.totalExtras ?: -1

        // check for end of innings
        val result = if (currentExtras >= targetExtras) "Yes" else "No"
        return QuestionResolution(true, result)
    }
}
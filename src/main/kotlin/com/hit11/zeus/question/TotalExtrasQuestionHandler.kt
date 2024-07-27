package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class TotalExtrasQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetExtras == null || question.targetExtras <= 0) {
            throw QuestionValidationException("Target extras must be a positive number for Total Extras question")
        }
    }
    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        return true
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetExtras = question.targetExtras ?: return QuestionResolution(false, null)
        val currentExtras = matchState.liveScorecard.innings.totalExtras

        val isResolved = currentExtras >= targetExtras
        val result = if (isResolved) "Yes" else "No"

        return QuestionResolution(isResolved, result)
    }
}
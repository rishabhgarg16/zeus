package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class SixesInMatchQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetSixes == null || question.targetSixes <= 0) {
            throw QuestionValidationException("Target sixes must be a positive number for Sixes in Match question")
        }
    }
    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        return true
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetSixes = question.targetSixes ?: return QuestionResolution(false, null)
        val currentSixes = matchState.currentInning.totalSixes

        val isResolved = currentSixes >= targetSixes
        val result = if (isResolved) "Yes" else "No"

        return QuestionResolution(isResolved, result)
    }
}
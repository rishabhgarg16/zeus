package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class MatchWinnerQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.optionA.isBlank() || question.optionB.isBlank()) {
            throw QuestionValidationException("Both team options must be provided for Match Winner question")
        }
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean =
        matchState.liveScorecard.state == "Complete"

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (canBeResolved(question, matchState)) {
            val result = if (matchState.liveScorecard.result.winningTeamId == question.targetTeamId) "Yes" else "No"
            return QuestionResolution(true, result)
        }
        return QuestionResolution(false, null)
    }
}
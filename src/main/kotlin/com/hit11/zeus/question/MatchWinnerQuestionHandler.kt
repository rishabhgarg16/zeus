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
        matchState.liveScorecard.status == "Complete"

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
//        return if (canBeResolved(question, matchState)) {
//            val result = if (matchState.liveScorecard.result.winner == question.optionA) "Yes" else "No"
//            QuestionResolution(true, result)
//        } else {
//            QuestionResolution(false, null)
//        }
        return QuestionResolution(false, null)
    }
}
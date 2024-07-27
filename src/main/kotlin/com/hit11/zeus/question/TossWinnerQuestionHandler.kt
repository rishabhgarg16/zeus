package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class TossWinnerQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetTeamId == 0) {
            throw QuestionValidationException("Target team must be specified for Toss Result question")
        }
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean =
        matchState.liveScorecard.tossResult != null

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (canBeResolved(question, matchState)) {
            val tossResult = matchState.liveScorecard.tossResult!!
            val result = if(tossResult.tossWinnerTeamId == question.targetTeamId) "Yes" else "No"
            return QuestionResolution(true, result)
        }
        return QuestionResolution(false, null)
    }
}
package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class TossDecisionQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetTossDecision.isNullOrBlank()) {
            throw QuestionValidationException("Target decision must be specified for Toss Decision question")
        }
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean =
        matchState.liveScorecard.tossResult != null

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (canBeResolved(question, matchState)) {
            val tossResult = matchState.liveScorecard.tossResult!!
            val result =
                if (question.targetTossDecision.equals(tossResult.tossDecision, ignoreCase = true)) "Yes" else "No"
            return QuestionResolution(true, result)
        }
        return QuestionResolution(false, null)
    }
}
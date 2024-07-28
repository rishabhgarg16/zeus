package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class ManOfTheMatchQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetBatsmanId == 0) {
            throw QuestionValidationException("Target player must be specified for Man of the Match question")
        }
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean =
        matchState.liveScorecard.state == "Complete" && matchState.liveScorecard.playerOfTheMatch != null

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (canBeResolved(question, matchState)) {
            val result = if (matchState.liveScorecard.playerOfTheMatch?.id == question.targetBatsmanId) "Yes" else "No"
            return QuestionResolution(true, result)
        }
        return QuestionResolution(false, null)
    }
}
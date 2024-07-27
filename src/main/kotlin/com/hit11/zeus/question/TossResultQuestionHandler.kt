package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class TossResultQuestionHandler : QuestionHandler {
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
            val isCorrectTeam = tossResult.tossWinnerTeamId == question.targetTeamId
            val result = when {
                isCorrectTeam && question.optionA.equals(tossResult.tossDecision, ignoreCase = true) -> "A"
                isCorrectTeam && question.optionB.equals(tossResult.tossDecision, ignoreCase = true) -> "B"
                else -> "Neither"
            }
            return QuestionResolution(true, result)
        }
        return QuestionResolution(false, null)
    }
}
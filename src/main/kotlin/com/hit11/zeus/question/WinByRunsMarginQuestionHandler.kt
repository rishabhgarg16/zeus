package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class WinByRunsMarginQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetTeamId == 0 || question.targetRuns == null) {
            throw QuestionValidationException("Target team and runs margin must be specified for Win by Runs Margin question")
        }
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean =
        matchState.liveScorecard.state == "Complete"

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (canBeResolved(question, matchState)) {
            val result = matchState.liveScorecard.result
            val isCorrectTeam = result.winningTeamId == question.targetTeamId
            val isCorrectMargin = result.winByRuns && result.winningMargin > question.targetRuns!!
            return QuestionResolution(true, if (isCorrectTeam && isCorrectMargin) "Yes" else "No")
        }
        return QuestionResolution(false, null)
    }
}
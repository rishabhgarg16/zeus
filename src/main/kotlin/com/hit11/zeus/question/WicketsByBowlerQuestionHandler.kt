package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class WicketsByBowlerQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetBowlerId == null) {
            throw QuestionValidationException("Target bowler ID is required for Wickets by Bowler question")
        }
        if (question.targetWickets == null || question.targetWickets <= 0) {
            throw QuestionValidationException("Target wickets must be a positive number for Wickets by Bowler question")
        }
        if (question.targetOvers != null && question.targetOvers <= 0) {
            throw QuestionValidationException("Target overs, if specified, must be a positive number for Wickets by Bowler question")
        }
    }
    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        val targetOvers = question.targetOvers
        val currentOver = matchState.currentBallEvent.overNumber
        return when {
            targetOvers != null -> currentOver >= targetOvers
            matchState.liveScorecard.status == "Complete" -> true
            else -> true
        }
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
//        if (!canBeResolved(question, matchState)) {
//            return QuestionResolution(false, null)
//        }
//
//        val bowlerPerformance = matchState.liveScorecard.innings.
//            .find { it.playerId == question.targetBowlerId }
//        val isResolved = bowlerPerformance?.wickets?.let { it >= question.targetWickets } ?: false
//        val result = if (isResolved) "Yes" else "No"
//
//        return QuestionResolution(isResolved, result)
        return QuestionResolution(false, null)
    }
}

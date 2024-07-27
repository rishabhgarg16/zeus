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
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        return true
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (!canBeResolved(question, matchState)) {
            return QuestionResolution(false, null)
        }

        val targetBowlerId = question.targetBowlerId ?: return QuestionResolution(false, null)
        val targetWickets = question.targetWickets ?: return QuestionResolution(false, null)

        val currentInnings = matchState.liveScorecard.innings
        val bowlerPerformance = currentInnings.bowlingPerformances.find { it.playerId == targetBowlerId }

        if (bowlerPerformance == null) {
            return QuestionResolution(false, null)
        }

        val wicketsTaken = bowlerPerformance.wickets

        val isResolved = wicketsTaken >= targetWickets
        val result = if (isResolved) "Yes" else "No"

        return QuestionResolution(isResolved, result)
    }
}

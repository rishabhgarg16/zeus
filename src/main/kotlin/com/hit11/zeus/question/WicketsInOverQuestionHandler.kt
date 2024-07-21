package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class WicketsInOverQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetSpecificOver == null || question.targetSpecificOver < 0) {
            throw QuestionValidationException("Target over must be a non-negative number for Wickets in Over question")
        }
        if (question.targetWickets == null || question.targetWickets <= 0) {
            throw QuestionValidationException("Target wickets must be a positive number for Wickets in Over question")
        }
        if (question.targetBowlerId == null) {
            throw QuestionValidationException("Target bowler ID is required for Wickets in Over question")
        }
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        val targetOver = question.targetSpecificOver ?: return false
        return matchState.currentBallEvent.overNumber > targetOver
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
//        if (!canBeResolved(question, matchState)) {
//            return QuestionResolution(false, null)
//        }
//
//        val targetOver = question.targetSpecificOver ?: return QuestionResolution(false, null)
//        val targetWickets = question.targetWickets ?: return QuestionResolution(false, null)
//
//        val wicketsInOver = matchState.currentInning.overs
//            .find { it.overNumber == targetOver }
//            ?.wickets ?: 0
//
//        val isResolved = wicketsInOver >= targetWickets
//        val result = if (isResolved) "Yes" else "No"
//
//        return QuestionResolution(isResolved, result)
        return QuestionResolution(false, null)
    }
}
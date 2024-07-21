package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class CenturyByBatsmanQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetBatsmanId == null) {
            throw QuestionValidationException("Target batsman ID is required for Century by Batsman question")
        }
    }
    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        return true // This can be resolved at any time during the match
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
//        val targetBatsmanId = question.targetBatsmanId ?: return QuestionResolution(false, null)
//        val batsmanPerformance = matchState.currentInning.battingPerformances
//            .find { it.playerId == targetBatsmanId }
//
//        val isResolved = batsmanPerformance?.runs?.let { it >= 100 } ?: false
//        val result = if (isResolved) "Yes" else "No"
//
//        return QuestionResolution(isResolved, result)
        return QuestionResolution(false, null)
    }
}
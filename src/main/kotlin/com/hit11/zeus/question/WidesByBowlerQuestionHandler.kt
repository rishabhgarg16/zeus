package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class WidesByBowlerQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetBowlerId == null) {
            throw QuestionValidationException("Target bowler ID is required for Wides by Bowler question")
        }
        if (question.targetWides == null || question.targetWides <= 0) {
            throw QuestionValidationException("Target wides must be a positive number for Wides by Bowler question")
        }
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        return true // This can be resolved at any time during the match
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (!canBeResolved(question, matchState)) {
            return QuestionResolution(false, null)
        }

        val targetBowlerId = question.targetBowlerId ?: return QuestionResolution(false, null)
        val targetWickets = question.targetWides ?: return QuestionResolution(false, null)

        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val bowlerPerformance = currentInnings?.bowlingPerformances?.find { it.playerId == targetBowlerId }

        if (bowlerPerformance == null) {
            return QuestionResolution(false, null)
        }

        val wicketsTaken = bowlerPerformance.wides
        val result = if (wicketsTaken >= targetWickets) "Yes" else "No"
        return QuestionResolution(true, result)
    }
}
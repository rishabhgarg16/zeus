package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

class SixesByPlayerQuestionHandler : QuestionHandler {
    override fun validate(question: QuestionDataModel) {
        if (question.targetSixes == null || question.targetSixes <= 0) {
            throw QuestionValidationException("Target sixes must be a positive number for Sixes in Match question")
        }

        if (question.targetBatsmanId == null || question.targetBatsmanId <= 0) {
            throw QuestionValidationException("Target sixes must be a BatsmanId in Match question")
        }
    }

    override fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean {
        return true
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        val targetSixes = question.targetSixes ?: return QuestionResolution(false, null)
        val targetBatsmanId = question.targetBatsmanId ?: return QuestionResolution(false, null)
        val currentInnings = matchState.liveScorecard.innings.find { it.isCurrentInnings }
        val currentSixes =
            currentInnings?.battingPerformances?.find { it.playerId == targetBatsmanId }?.sixes ?: 0

        // check if player got out or innings over
        val result = if (currentSixes >= targetSixes) "Yes" else "No"

        return QuestionResolution(true, result)
    }
}
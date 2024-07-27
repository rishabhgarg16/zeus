package com.hit11.zeus.question

import com.hit11.zeus.exception.QuestionValidationException
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel
import java.math.BigDecimal

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
        val targetOvers = question.targetOvers
        val currentOver = matchState.liveScorecard.innings.overs
        return when {
            targetOvers != null -> currentOver.toInt() + 1 == targetOvers
            else -> false
        }
    }

    override fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution {
        if (!canBeResolved(question, matchState)) {
            return QuestionResolution(false, null)
        }

        val targetOver = question.targetSpecificOver ?: return QuestionResolution(false, null)
        val targetWickets = question.targetWickets ?: return QuestionResolution(false, null)
        val targetBowlerId = question.targetBowlerId ?: return QuestionResolution(false, null)

        val wicketsInOver = countWicketsInOver(matchState.liveScorecard, targetBowlerId)
        val isResolved = wicketsInOver >= targetWickets
        val result = if (isResolved) "Yes" else "No"

        return QuestionResolution(isResolved, result)
    }

    private fun countWicketsInOver(scorecard: Hit11Scorecard, targetBowlerId: Int): Int {
        val commentaryList = scorecard.innings.ballByBallEvents
        val bowlerId = scorecard.innings.bowlingPerformances.find {it.onStrike == 1}?.playerId ?: -1
        if(targetBowlerId != bowlerId) {
            throw QuestionValidationException("Target bowler ID is different to bolwer on strike")
        }
        var wicketCount = 0
        val overNumber =

        for (event in commentaryList.reversed()) {
            val eventOver = event.overNumber.toInt() + 1

            when {
                eventOver > BigDecimal(targetOver) -> continue
                eventOver < BigDecimal(targetOver - 1) -> break  // Break if we've gone past the previous over
                eventOver.toInt() == targetOver - 1 && eventOver.remainder(BigDecimal.ONE) > BigDecimal("0.5") -> {
                    // Count wickets in the last ball of the previous over (X.6)
                    if (event.isWicket && isBowlerWicket(event.commText)) {
                        wicketCount++
                    }
                }
                eventOver.toInt() == targetOver -> {
                    // Count wickets in the target over
                    if (event.isWicket && isBowlerWicket(event.commText)) {
                        wicketCount++
                    }
                }
            }
        }

        return wicketCount
    }

    private fun parseOver(overString: String): BigDecimal {
        return try {
            BigDecimal(overString)
        } catch (e: NumberFormatException) {
            BigDecimal.ZERO
        }
    }
}
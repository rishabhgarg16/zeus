package com.hit11.zeus.question

import com.hit11.zeus.model.MatchState
import com.hit11.zeus.model.QuestionDataModel

interface QuestionHandler {
    fun validate(question: QuestionDataModel)
    fun canBeResolved(question: QuestionDataModel, matchState: MatchState): Boolean
    fun resolveQuestion(question: QuestionDataModel, matchState: MatchState): QuestionResolution
}

data class QuestionResolution(
    val isResolved: Boolean,
    val result: String?
)
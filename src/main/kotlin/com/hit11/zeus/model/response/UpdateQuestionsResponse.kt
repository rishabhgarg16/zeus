package com.hit11.zeus.model.response

import com.hit11.zeus.model.QuestionDataModel

data class UpdateQuestionsResponse(
    val updatedQuestions: List<QuestionDataModel> = emptyList(),
    val notUpdatedQuestions: List<QuestionDataModel> = emptyList(),
    val errors: List<String> = emptyList()
)

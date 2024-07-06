package com.hit11.zeus.model.response

import com.hit11.zeus.model.QuestionDataModel

data class UpdateQuestionsResponse(
    val updatedQuestions: List<QuestionDataModel>,
    val notUpdatedQuestions: List<QuestionDataModel>,
    val errors: List<String>
)

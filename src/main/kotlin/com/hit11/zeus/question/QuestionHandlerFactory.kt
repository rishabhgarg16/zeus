package com.hit11.zeus.question

import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.QuestionType

object QuestionHandlerFactory {
    private val handlers = mapOf(
        QuestionType.MATCH_WINNER to MatchWinnerQuestionHandler(),
        QuestionType.TEAM_RUNS_IN_MATCH to TeamRunsInMatchQuestionHandler(),
        QuestionType.SIXES_IN_MATCH to SixesByPlayerQuestionHandler(),
        QuestionType.RUNS_SCORED_BY_BATSMAN to RunsScoredByBatsmanQuestionHandler(),
        QuestionType.WICKETS_BY_BOWLER to WicketsByBowlerQuestionHandler(),
        QuestionType.WICKETS_IN_OVER to WicketsInOverQuestionHandler(),
        QuestionType.WIDES_IN_MATCH to WidesByBowlerQuestionHandler(),
        QuestionType.TOTAL_EXTRAS to TotalExtrasQuestionHandler(),
        QuestionType.MAN_OF_THE_MATCH to ManOfTheMatchQuestionHandler(),
        QuestionType.WIN_BY_RUNS_MARGIN to WinByRunsMarginQuestionHandler(),
        QuestionType.TOSS_WINNER to TossWinnerQuestionHandler(),
        QuestionType.TOSS_DECISION to TossDecisionQuestionHandler()
    )

    fun getHandler(question: QuestionDataModel): QuestionHandler {
        val handler = handlers[question.questionType]
            ?: throw IllegalArgumentException("No handler found for question type: ${question.questionType}")
        handler.validate(question)
        return handler
    }

}
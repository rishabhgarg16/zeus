package com.hit11.zeus.service

import com.hit11.zeus.adapter.OrderAdapter.toTradeResponse
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.*
import com.hit11.zeus.model.response.UpdateQuestionsResponse
import com.hit11.zeus.repository.BatsmanPerformanceRepository
import com.hit11.zeus.repository.BowlerPerformanceRepository
import com.hit11.zeus.repository.QuestionRepository
import com.hit11.zeus.repository.ScoreRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service class QuestionService(
    private val orderService: OrderService,
    private val userService: UserService,
    private val questionRepository: QuestionRepository,
    private val bowlerPerformanceRepository: BowlerPerformanceRepository,
    private val batsmanPerformanceRepository: BatsmanPerformanceRepository,
    private val scoreRepository: ScoreRepository,
) {
    private val logger = Logger.getLogger(QuestionService::class.java)
    fun getAllActiveQuestions(matchIdList: List<Int>): List<QuestionDataModel>? {
        val activePulse = questionRepository.findByMatchIdInAndStatus(
            matchIdList,
            true
        )
        return activePulse?.map { it.mapToQuestionDataModel() }
    }

    @Transactional fun submitOrder(response: OrderDataModel) {
        // check pulse is not expired
//        val pulse = pulseRepositorySql.findByPulseId(response.pulseId)
        val amountToDeduct = "%.2f".format(response.tradeAmount).toDouble()
        try {
            val balanceSuccess = userService.updateBalance(
                response.userId,
                -amountToDeduct
            )

            if (balanceSuccess) {
                orderService.saveOrder(response)
            } else {
                logger.error("Error updating the user wallet for user id ${response.userId}")
                throw RuntimeException("Error updating the user wallet for user id ${response.userId}")
            }
        } catch (e: Exception) {
            logger.error(
                "Error in submitUserTrade for user id ${response.userId}",
                e
            )
            throw e
        }
    }

    fun getAllTradesByUserAndMatch(
        userId: Int,
        matchIdList: List<Int>
    ): List<TradeResponse>? {
        try {
            val allTrades = orderService.getAllTradesByUserIdAndMatchIdIn(
                userId,
                matchIdList
            )
            val matchQuestions = questionRepository.findAllByMatchIdIn(matchIdList).map { it.mapToQuestionDataModel() }

            val questionIdToQuestionMap: Map<Int, QuestionDataModel> =
                    matchQuestions.associateBy { it.id }.mapValues { it.value }

            return allTrades?.mapNotNull { trade ->
                questionIdToQuestionMap[trade.pulseId]?.let {
                    trade.toTradeResponse(
                        it
                    )
                }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun updatePulseAnswer(answerUpdateRequest: PulseAnswerUpdateRequest): PulseAnswerUpdateResponse {
        val res = PulseAnswerUpdateResponse()
        try {
            val pulse = questionRepository.getPulseById(answerUpdateRequest.pulseId)
            pulse.pulseResult = answerUpdateRequest.pulseResult
            pulse.status = false
            questionRepository.save(pulse)

            val orders = orderService.getAllTradesByPulseId(answerUpdateRequest.pulseId)
            orders?.forEach {
                if (it.userAnswer == answerUpdateRequest.pulseResult) {
                    it.userResult = "Win"
                    userService.updateBalance(
                        it.userId,
                        it.quantity * 10.0
                    )
                } else {
                    it.userResult = "Lose"
                }
                orderService.saveOrder(it)
            }
        } catch (e: Exception) {
            throw e
        }
        return res
    }

    fun updateQuestions(
        score: Score,
        bowlerPerformance: BowlerPerformance,
        batsmanPerformance: BatsmanPerformance
    ): UpdateQuestionsResponse {
        val questions = questionRepository.findByMatchIdInAndStatus(
            listOf(score.matchId),
            true
        )?.map { it.mapToQuestionDataModel() }

        val updatedQuestions = mutableListOf<QuestionDataModel>()
        val notUpdatedQuestions = mutableListOf<QuestionDataModel>()
        val errors = mutableListOf<String>()

        questions?.forEach { question ->
            var questionUpdated = false
            try {
                questionUpdated = handleQuestionUpdate(
                    question,
                    batsmanPerformance,
                    bowlerPerformance,
                    score
                )
                updatedQuestions.add(question)
            } catch (e: Exception) {
                notUpdatedQuestions.add(question)
                val errorMessage = "Error updating question id ${question.id}: ${e.message}"
                errors.add(errorMessage)
                logger.error(
                    errorMessage,
                    e
                )
            }

            if (questionUpdated) {
                updatedQuestions.add(question)
                questionRepository.save(question.maptoEntity())
            } else {
                notUpdatedQuestions.add(question)
            }
        }

        return UpdateQuestionsResponse(
            updatedQuestions,
            notUpdatedQuestions,
            errors
        )

    }

    private fun handleQuestionUpdate(
        question: QuestionDataModel,
        batsmanPerformance: BatsmanPerformance,
        bowlerPerformance: BowlerPerformance,
        score: Score
    ): Boolean {
        return when (question.questionType) {
            QuestionType.MATCH_WINNER -> handleMatchWinnerQuestion(
                question,
                score
            )

            QuestionType.TEAM_RUNS_IN_MATCH -> handleTeamRunsInMatchQuestion(
                question,
                score
            )

            QuestionType.SIXES_IN_MATCH -> handleSixesInMatchQuestion(
                question,
                score
            )

            QuestionType.CENTURY_BY_BATSMAN -> handleCenturyByBatsmanQuestion(
                question,
                batsmanPerformance,
                score
            )

            QuestionType.WICKETS_IN_OVER -> handleWicketsInOverQuestion(
                question,
                bowlerPerformance,
                score
            )

            QuestionType.WICKETS_BY_BOWLER -> handleWicketByBowlerQuestion(
                question,
                bowlerPerformance,
                score
            )

            QuestionType.WIDES_IN_MATCH -> handleWidesByBowlerQuestion(
                question,
                bowlerPerformance,
                score
            )

            QuestionType.TOTAL_EXTRAS -> handleTotalExtrasQuestion(
                question,
                score
            )

            else -> {
                logger.error("Invalid question type ${question.questionType}")
                throw RuntimeException("Invalid question type ${question.questionType}")
            }
        }
    }

    private fun handleMatchWinnerQuestion(
        question: QuestionDataModel,
        score: Score
    ): Boolean {
        // Implementation to check if the match is won by the specified team
        // This will be updated at the end of the match
        return false
    }

    private fun handleTeamRunsInMatchQuestion(
        question: QuestionDataModel,
        score: Score
    ): Boolean {
        val targetRuns = question.targetRuns!!

//        val totalRuns = calculateTotalRuns(
//            score.matchId,
//            score.inningId
//        )
        val totalRuns = score.totalRuns
        if (totalRuns >= targetRuns) {
            return true
        } else {
            return false
        }
    }

    private fun handleWicketByBowlerQuestion(
        question: QuestionDataModel,
        bowlerPerformance: BowlerPerformance,
        score: Score
    ): Boolean {
        require(question.targetBowlerId != null) { " targetBowlerId should not be null for question ${question.id}" }
        val bowlerId = question.targetBowlerId
        if (score.isWicket && score.bowlerId == bowlerId) {
//            val wicketsTaken = calculateWicketsTakenByPlayer(
//                score.bowlerId,
//                score.matchId
//            )
            val wicketsTaken = bowlerPerformance.wicketsTaken
            if (wicketsTaken >= question.targetWickets!!) {
                return true
            }
        }
        return false
    }

    private fun handleWicketsInOverQuestion(
        question: QuestionDataModel,
        bowlerPerformance: BowlerPerformance,
        score: Score
    ): Boolean {
        require(question.targetWickets != null) { " targetWickets should not be null for question ${question.id}" }
        require(question.targetBowlerId != null) { " targetBowlerId should not be null for question ${question.id}" }

        if (score.overNumber == question.targetSpecificOver &&
            score.isWicket &&
            score.bowlerId == question.targetBowlerId
        ) {
            val wicketsInOver = calculateWicketsInOver(
                score
            )
            if (1 + wicketsInOver == question.targetWickets) {
                return true
            }
        }
        return false
    }

    private fun handleWidesByBowlerQuestion(
        question: QuestionDataModel,
        bowlerPerformance: BowlerPerformance,
        score: Score
    ): Boolean {
        require(question.targetBowlerId != null) { " targetBowlerId should not be null for question ${question.id}" }
        require(question.targetWides != null) { " targetWides should not be null for question ${question.id}" }

        if (score.isWide && score.bowlerId == question.targetBowlerId) {
//            val widesBowled = calculateWidesBowledByPlayer(
//                score.bowlerId,
//                score.matchId
//            )
            val widesBowled = bowlerPerformance.wides
            if (widesBowled == question.targetWides) {
                return true
            }
        }
        return false
    }

    private fun handleTotalExtrasQuestion(
        question: QuestionDataModel,
        score: Score
    ): Boolean {
        require(question.targetExtras != null) { " targetExtras cannot be null for question id ${question.id}" }

        if (score.totalExtras >= question.targetExtras) {
            return true
        }
        return false
    }

    private fun handleCenturyByBatsmanQuestion(
        question: QuestionDataModel,
        batsmanPerformance: BatsmanPerformance,
        score: Score
    ): Boolean {
        require(question.targetBatsmanId != null) { " targetBatsmanId cannot be null for question id ${question.id}" }
        if (score.batsmanId == question.targetBatsmanId) {
//            val runsScored = calculateRunsScoredByPlayer(
//                score.batsmanId,
//                score.matchId
//            )
            if (batsmanPerformance.runsScored >= 100) {
                return true
            }
        }
        return false
    }

    private fun handleSixesInMatchQuestion(
        question: QuestionDataModel,
        score: Score
    ): Boolean {
        require(question.targetSixes != null) { " targetSixes cannot be null for question id ${question.id}" }
        if (score.isSix) {
            val totalSixes = calculateTotalSixes(
                score.matchId,
                score.inningId
            )
            if (1 + totalSixes == question.targetSixes) {
                return true
            }
        }
        return false
    }

    private fun handleRunsScoredByBatsmanQuestion(
        question: QuestionDataModel,
        batsmanPerformance: BatsmanPerformance,
        score: Score
    ): Boolean {
        require(question.targetBatsmanId != null) { " targetBatsmanId cannot be null for question id ${question.id}" }
        if (score.batsmanId == question.targetBatsmanId) {
            val runsScored = calculateRunsScoredByPlayer(
                score.batsmanId,
                score.matchId
            )
            if (runsScored + batsmanPerformance.runsScored >= (question.targetRuns ?: return false)) {
                return true
            }
        }
        return false
    }

    private fun calculateTotalSixes(
        matchId: Int,
        inningId: Int
    ): Int {
        // TODO store total sixes as well
        val score = scoreRepository.findByMatchIdAndInningId(
            matchId,
            inningId
        )

        return score?.count { it.isSix } ?: 0
    }


    private fun calculateTotalRuns(
        matchId: Int,
        inningId: Int
    ): Int {
        val score = scoreRepository.findTopByMatchIdAndInningIdOrderByOverNumberDescBallNumberDesc(
            matchId,
            inningId
        )

        return score?.totalRuns ?: 0
    }

    private fun calculateWicketsTakenByPlayer(
        bowlerId: Int,
        matchId: Int
    ): Int {
        val performances = bowlerPerformanceRepository.findByMatchIdAndPlayerId(
            matchId,
            bowlerId
        )
        return performances?.wicketsTaken ?: 0
    }

    private fun calculateWicketsInOver(
        score: Score,
    ): Int {
        val wickets = scoreRepository.findWicketsByMatchIdAndBowlerIdAndOverNumber(
            score.matchId,
            score.bowlerId,
            score.overNumber
        )
        return wickets
    }

    private fun calculateWidesBowledByPlayer(
        bowlerId: Int,
        matchId: Int
    ): Int {
        val performances = bowlerPerformanceRepository.findByMatchIdAndPlayerId(
            matchId,
            bowlerId
        )
        return performances?.wides ?: 0
    }

    private fun calculateTotalExtras(
        matchId: Int,
        inningId: Int
    ): Int {
        val scores = scoreRepository.findTopByMatchIdAndInningIdOrderByOverNumberDescBallNumberDesc(
            matchId,
            inningId
        )
        return scores?.totalExtras ?: 0
    }

    private fun calculateRunsScoredByPlayer(
        batsmanId: Int,
        matchId: Int
    ): Int {
        val performance = batsmanPerformanceRepository.findByMatchIdAndPlayerId(
            matchId,
            batsmanId
        )
        return performance?.runsScored ?: 0
    }
}

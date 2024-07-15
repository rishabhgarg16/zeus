package com.hit11.zeus.service

import com.hit11.zeus.adapter.OrderAdapter.toTradeResponse
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.BattingPerformance
import com.hit11.zeus.livedata.BowlingPerformance
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.*
import com.hit11.zeus.model.response.UpdateQuestionsResponse
import com.hit11.zeus.repository.BallEventRepository
import com.hit11.zeus.repository.BatsmanPerformanceRepository
import com.hit11.zeus.repository.BowlerPerformanceRepository
import com.hit11.zeus.repository.QuestionRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class QuestionService(
    private val orderService: OrderService,
    private val userService: UserService,
    private val questionRepository: QuestionRepository,
    private val bowlerPerformanceRepository: BowlerPerformanceRepository,
    private val batsmanPerformanceRepository: BatsmanPerformanceRepository,
    private val ballEventRepository: BallEventRepository,
) {
    private val logger = Logger.getLogger(QuestionService::class.java)
    fun getAllActiveQuestions(matchIdList: List<Int>): List<QuestionDataModel>? {
        val activePulse = questionRepository.findByMatchIdInAndStatus(
            matchIdList,
            true
        )
        return activePulse?.map { it.mapToQuestionDataModel() }
    }

    @Transactional
    fun submitOrder(response: OrderDataModel) {
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
        ballEventEntity: BallEventEntity,
        currentInning: Inning,
        liveScoreEvent: Hit11Scorecard
    ): UpdateQuestionsResponse {
        val questions = questionRepository.findByMatchIdInAndStatus(
            listOf(ballEventEntity.matchId),
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
                    currentInning,
                    liveScoreEvent,
                    ballEventEntity
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
        currentInning: Inning,
        liveScoreEvent: Hit11Scorecard,
        ballEventEntity: BallEventEntity
    ): Boolean {
        return when (question.questionType) {
            QuestionType.MATCH_WINNER -> handleMatchWinnerQuestion(
                question,
                liveScoreEvent
            )

            QuestionType.TEAM_RUNS_IN_MATCH -> handleTeamRunsInMatchQuestion(
                question,
                currentInning.battingTeamId,
                currentInning.totalRuns
            )

            QuestionType.SIXES_IN_MATCH -> handleSixesInMatchQuestion(
                question,
                currentInning
            )

            QuestionType.CENTURY_BY_BATSMAN -> handleCenturyByBatsmanQuestion(
                question,
                liveScoreEvent.innings[currentInning.inningsNumber].battingPerformances,
                ballEventEntity.batsmanId,
            )

            QuestionType.WICKETS_IN_OVER -> handleWicketsInOverQuestion(
                question,
                liveScoreEvent.innings[currentInning.inningsNumber].bowlingPerformances,
                ballEventEntity
            )

            QuestionType.WICKETS_BY_BOWLER -> handleWicketByBowlerQuestion(
                question,
                liveScoreEvent.innings[currentInning.inningsNumber].bowlingPerformances,
                ballEventEntity
            )

            QuestionType.WIDES_IN_MATCH -> handleWidesByBowlerQuestion(
                question,
                liveScoreEvent.innings[currentInning.inningsNumber].bowlingPerformances,
                ballEventEntity
            )

            QuestionType.TOTAL_EXTRAS -> handleTotalExtrasQuestion(
                question,
                currentInning
            )

            else -> {
                logger.error("Invalid question type ${question.questionType}")
                throw RuntimeException("Invalid question type ${question.questionType}")
            }
        }
    }

    // FIXME: Implement the rest of the question handling methods
    private fun handleMatchWinnerQuestion(
        question: QuestionDataModel,
        liveScoreEvent: Hit11Scorecard
    ): Boolean {
        // Implementation to check if the match is won by the specified team
        // This will be updated at the end of the match
        // liveScoreEvent.result.winner
        return false
    }

    // FIXME: Implement the rest of the question handling methods
    private fun handleTeamRunsInMatchQuestion(
        question: QuestionDataModel,
        ballEventEntity: Int,
        totalRuns: Int
    ): Boolean {
        val targetRuns = question.targetRuns!!

//        val totalRuns = calculateTotalRuns(
//            score.matchId,
//            score.inningId
//        )

        if (totalRuns >= targetRuns) {
            return true
        } else {
            return false
        }
    }

    private fun handleWicketByBowlerQuestion(
        question: QuestionDataModel,
        bowlerPerformance: List<BowlingPerformance>,
        ballEventEntity: BallEventEntity
    ): Boolean {
        require(question.targetBowlerId != null) { " targetBowlerId should not be null for question ${question.id}" }
        require(question.targetWickets != null) { " targetWickets should not be null for question ${question.id}" }
        val bowlerId = question.targetBowlerId
        val bowlerPerf = bowlerPerformance.firstOrNull { it.playerId == bowlerId }

        if (ballEventEntity.isWicket && ballEventEntity.bowlerId == bowlerId) {
            val wicketsTaken = bowlerPerf?.wides ?: -1
            if (wicketsTaken >= question.targetWickets) {
                return true
            }
        }

        return false
    }

    private fun handleWicketsInOverQuestion(
        question: QuestionDataModel,
        bowlerPerformance: List<BowlingPerformance>,
        ballEventEntity: BallEventEntity
    ): Boolean {
        require(question.targetWickets != null) { " targetWickets should not be null for question ${question.id}" }
        require(question.targetBowlerId != null) { " targetBowlerId should not be null for question ${question.id}" }

        if (ballEventEntity.overNumber == question.targetSpecificOver &&
            ballEventEntity.isWicket &&
            ballEventEntity.bowlerId == question.targetBowlerId
        ) {
            val wicketsInOver = calculateWicketsInOver(
                ballEventEntity
            )
            if (1 + wicketsInOver == question.targetWickets) {
                return true
            }
        }
        return false
    }

    private fun handleWidesByBowlerQuestion(
        question: QuestionDataModel,
        bowlerPerformance: List<BowlingPerformance>,
        ballEventEntity: BallEventEntity
    ): Boolean {
        require(question.targetBowlerId != null) { " targetBowlerId should not be null for question ${question.id}" }
        require(question.targetWides != null) { " targetWides should not be null for question ${question.id}" }
        val bPerf = bowlerPerformance.firstOrNull { it.playerId == question.targetBowlerId }
        if (ballEventEntity.isWide && ballEventEntity.bowlerId == question.targetBowlerId) {
            val widesBowled = bPerf?.wides
            if (widesBowled == question.targetWides) {
                return true
            }
        }

        return false
    }

    private fun handleTotalExtrasQuestion(
        question: QuestionDataModel,
        inning: Inning
    ): Boolean {
        require(question.targetExtras != null) { " targetExtras cannot be null for question id ${question.id}" }

        if (inning.totalExtras >= question.targetExtras) {
            return true
        }
        return false
    }

    private fun handleCenturyByBatsmanQuestion(
        question: QuestionDataModel,
        batsmanPerformance: List<BattingPerformance>,
        ballEventEntity: Int
    ): Boolean {
        require(question.targetBatsmanId != null) { " targetBatsmanId cannot be null for question id ${question.id}" }
        val batsmanId = question.targetBatsmanId
        val batsmanPerf = batsmanPerformance.firstOrNull { it.playerId == batsmanId }
        if (batsmanPerf?.playerId == question.targetBatsmanId) {
            if (batsmanPerf.runs >= 100) {
                return true
            }
        }
        return false
    }

    private fun handleSixesInMatchQuestion(
        question: QuestionDataModel,
        inning: Inning
    ): Boolean {
        require(question.targetSixes != null) { " targetSixes cannot be null for question id ${question.id}" }
        if (1 + inning.totalSixes >= question.targetSixes) {
            return true
        }
        return false
    }

    private fun calculateWicketsInOver(
        ballEventEntity: BallEventEntity,
    ): Int {
        val wickets = ballEventRepository.findWicketsByMatchIdAndBowlerIdAndOverNumber(
            ballEventEntity.matchId,
            ballEventEntity.bowlerId,
            ballEventEntity.overNumber
        )
        return wickets
    }

}

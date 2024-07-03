package com.hit11.zeus.service

import com.hit11.zeus.adapter.OrderAdapter.toTradeResponse
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.*
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service class QuestionService(
    private val orderService: OrderService,
    private val userService: UserService,
    private val questionRepository: QuestionRepository,
    private val bowlerPerformanceRepository: BowlerPerformanceRepository,
    private val batsmanPerformanceRepository: BatsmanPerformanceRepository,
    private val matchRepository: MatchRepository,
    private val inningRepository: InningRepository,
    private val scoreRepository: ScoreRepository,
    private val ballEventRepository: BallEventRepository
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

    fun updateQuestions(ballEvent: BallEvent) {
        val questions = questionRepository.findByMatchIdInAndStatus(
            listOf(ballEvent.matchId),
            true
        )?.map { it.mapToQuestionDataModel() }

        questions?.forEach { question ->
            var questionUpdated = false
            try {
                when (question.type) {
                    QuestionType.MATCH_WINNER -> questionUpdated = handleMatchWinnerQuestion(
                        question,
                        ballEvent
                    )

                    QuestionType.TEAM_RUNS_IN_MATCH -> questionUpdated = handleTeamRunsInMatchQuestion(
                        question,
                        ballEvent
                    )

                    QuestionType.SUPER_OVER_IN_MATCH -> TODO()
                    QuestionType.TOP_SCORER -> TODO()
                    QuestionType.SIXES_IN_MATCH -> TODO()
                    QuestionType.CENTURY_IN_MATCH -> questionUpdated = handleCenturyInMatchQuestion(
                        question,
                        ballEvent
                    )

                    QuestionType.WICKETS_IN_OVER -> questionUpdated = handleWicketsInOverQuestion(
                        question,
                        ballEvent
                    )

                    QuestionType.WICKETS_IN_MATCH -> TODO()
                    QuestionType.WICKETS_BY_BOWLER -> questionUpdated = handleWicketByBowlerQuestion(
                        question,
                        ballEvent
                    )

                    QuestionType.ECONOMY_RATE -> TODO()
                    QuestionType.WIDES_IN_MATCH -> questionUpdated = handleWidesInMatchQuestion(
                        question,
                        ballEvent
                    )

                    QuestionType.TOTAL_EXTRAS -> questionUpdated = handleTotalExtrasQuestion(
                        question,
                        ballEvent
                    )

                    QuestionType.INVALID -> TODO()

                    else -> {
                        logger.error("Invalid question type ${question.type}")
                        throw RuntimeException("Invalid question type ${question.type}")
                    }
                }
            } catch (e: Exception) {
                logger.error(
                    "Error in updateQuestions for question id ${question.id}",
                    e
                )
            }

            if (questionUpdated) {
                questionRepository.save(question.maptoEntity())
            }
        }
    }

    private fun handleMatchWinnerQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        // Implementation to check if the match is won by the specified team
        // This will be updated at the end of the match
        return false
    }

    private fun handleTeamRunsInMatchQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        val targetRuns = question.targetRuns!!

        val totalRuns = calculateTotalRuns(
            ballEvent.matchId,
            ballEvent.inningId
        )
        if (totalRuns >= targetRuns) {
            return true
        } else {
            return false
        }
    }

    private fun handleWicketByBowlerQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        val playerId = question.playerId!!
        if (ballEvent.isWicket && ballEvent.bowlerId == playerId) {
            val wicketsTaken = calculateWicketsTakenByPlayer(
                ballEvent.bowlerId,
                ballEvent.matchId
            )
            if (wicketsTaken >= question.targetWickets!!) {
                return true
            }
        }
        return false
    }

    private fun handleWicketsInOverQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        if (ballEvent.overNumber == question.specificOver &&
            ballEvent.isWicket &&
            ballEvent.bowlerId == question.playerId
        ) {
            val wicketsInOver = calculateWicketsInOver(
                ballEvent
            )
            if (wicketsInOver >= question.targetWickets!!) {
                return true
            }
        }
        return false
    }

    private fun handleWidesInMatchQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        if (ballEvent.isWide && ballEvent.bowlerId == question.playerId) {
            val widesBowled = calculateWidesBowledByPlayer(
                ballEvent.bowlerId,
                ballEvent.matchId
            )
            if (widesBowled >= question.targetExtras!!) {
                return true
            }
        }
        return false
    }

    private fun handleTotalExtrasQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        val totalExtras = calculateTotalExtras(
            ballEvent.matchId,
            ballEvent.inningId
        )
        if (totalExtras >= question.targetExtras!!) {
            return true
        }
        return false
    }

    private fun handleCenturyInMatchQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        if (ballEvent.batsmanId == question.playerId) {
            val runsScored = calculateRunsScoredByPlayer(
                ballEvent.batsmanId,
                ballEvent.matchId
            )
            if (runsScored >= 100) {
                return true
            }
        }
        return false
    }

    private fun handleSixesInMatchQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        val targetSixes = question.targetSixes!!
        if (ballEvent.runsScored == 6) {
            val totalSixes = calculateTotalSixes(ballEvent.matchId, ballEvent.inningId)
            if (totalSixes >= targetSixes) {
                return true
            }
        }
        return false
    }

    private fun handleRunsScoredByBatsmanQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        if (ballEvent.batsmanId == question.playerId) {
            val runsScored = calculateRunsScoredByPlayer(
                ballEvent.batsmanId,
                ballEvent.matchId
            )
            if (runsScored >= (question.targetRuns ?: return false)) {
                return true
            }
        }
        return false
    }

    private fun calculateTotalSixes(
        matchId: Int,
        inningId: Int
    ) : Int {
        val score = scoreRepository.findByMatchIdAndInningId(
            matchId,
            inningId
        )

        return score?.count { it.isSix } ?: -1
    }


    private fun calculateTotalRuns(
        matchId: Int,
        inningId: Int
    ): Int {
        val score = scoreRepository.findTopByMatchIdAndInningIdOrderByOverNumberDescBallNumberDesc(
            matchId,
            inningId
        )

        return score?.totalRuns ?: -1
    }

    private fun calculateWicketsTakenByPlayer(
        bowlerId: Int,
        matchId: Int
    ): Int {
        val performances = bowlerPerformanceRepository.findByMatchIdIdAndPlayerId(
            matchId,
            bowlerId
        )
        return performances?.wicketsTaken ?: -1
    }

    private fun calculateWicketsInOver(
        ballEvent: BallEvent,
    ): Int {
        val wickets = scoreRepository.findWicketsByOverNumber(
            ballEvent.matchId,
            ballEvent.bowlerId,
            ballEvent.overNumber
        )
        return wickets
    }

    private fun calculateWidesBowledByPlayer(
        bowlerId: Int,
        matchId: Int
    ): Int {
        val performances = bowlerPerformanceRepository.findByMatchIdIdAndPlayerId(
            matchId,
            bowlerId
        )
        return performances?.wides ?: -1
    }

    private fun calculateTotalExtras(
        matchId: Int,
        inningId: Int
    ): Int {
        val scores = scoreRepository.findTopByMatchIdAndInningIdOrderByOverNumberDescBallNumberDesc(
            matchId,
            inningId
        )
        return scores?.totalExtras ?: -1
    }

    private fun calculateRunsScoredByPlayer(
        batsmanId: Int,
        matchId: Int
    ): Int {
        val performance = batsmanPerformanceRepository.findByMatchIdIdAndPlayerId(
            matchId,
            batsmanId
        )
        return performance?.runsScored ?: -1
    }
}

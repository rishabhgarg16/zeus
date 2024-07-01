package com.hit11.zeus.service

import com.hit11.zeus.adapter.OrderAdapter.toTradeResponse
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class QuestionService(
    private val orderService: OrderService,
    private val userService: UserService,
    private val questionRepository: QuestionRepository,
) {
    private val logger = Logger.getLogger(QuestionService::class.java)
    fun getAllActiveQuestions(matchIdList: List<Int>): List<QuestionDataModel>? {
        val activePulse = questionRepository.findByMatchIdInAndStatus(matchIdList, true)
        return activePulse?.map { it.mapToQuestionDataModel() }
    }

    @Transactional
    fun submitOrder(response: OrderDataModel) {
        // check pulse is not expired
//        val pulse = pulseRepositorySql.findByPulseId(response.pulseId)
        val amountToDeduct = "%.2f".format(response.tradeAmount).toDouble()
        try {
            val balanceSuccess = userService.updateBalance(response.userId, -amountToDeduct)

            if (balanceSuccess) {
                orderService.saveOrder(response)
            } else {
                logger.error("Error updating the user wallet for user id ${response.userId}")
                throw RuntimeException("Error updating the user wallet for user id ${response.userId}")
            }
        } catch (e: Exception) {
            logger.error("Error in submitUserTrade for user id ${response.userId}", e)
            throw e
        }
    }

    fun getAllTradesByUserAndMatch(userId: Int, matchIdList: List<Int>): List<TradeResponse>? {
        try {
            val allTrades = orderService.getAllTradesByUserIdAndMatchIdIn(userId, matchIdList)
            val matchQuestions = questionRepository.findAllByMatchIdIn(matchIdList).map { it.mapToQuestionDataModel() }
            val questionIdToQuestionMap: Map<Int, QuestionDataModel> = matchQuestions.associateBy { it.id }.mapValues { it.value }
            return allTrades?.mapNotNull { trade -> questionIdToQuestionMap[trade.pulseId]?.let { trade.toTradeResponse(it) } }
        } catch (e: Exception) {
            throw e
        }
    }

    fun updatePulseAnswer(anserRequest: PulseAnswerUpdateRequest): PulseAnswerUpdateResponse {
        var res = PulseAnswerUpdateResponse()
        try {
            val pulse = questionRepository.getPulseById(anserRequest.pulseId)
            pulse.pulseResult = anserRequest.pulseResult
            pulse.status = false
            questionRepository.save(pulse)

            var orders = orderService.getAllTradesByPulseId(anserRequest.pulseId)
            orders?.forEach {
                if (it.userAnswer == anserRequest.pulseResult) {
                    it.userResult = "Win"
                    userService.updateBalance(it.userId, it.quantity * 10.0)
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
                    true)?.map { it.mapToQuestionDataModel() }

        questions?.forEach { question ->
            var questionUpdated = false

            when (question.type) {
                QuestionType.MATCH_WINNER -> questionUpdated = handleMatchWinnerQuestion(question, ballEvent)
                QuestionType.RUNS_IN_MATCH -> questionUpdated = handleRunsInOversQuestion(question, ballEvent)
                QuestionType.SUPER_OVER_IN_MATCH -> TODO()
                QuestionType.TOP_SCORER -> TODO()
                QuestionType.SIXES_IN_MATCH -> TODO()
                QuestionType.CENTURY_IN_MATCH -> questionUpdated = handleCenturyInMatchQuestion(question, ballEvent)
                QuestionType.TEAM_RUNS_IN_MATCH -> TODO()
                QuestionType.WICKETS_IN_OVER -> questionUpdated = handleWicketsInOverQuestion(question, ballEvent)
                QuestionType.WICKETS_IN_MATCH -> TODO()
                QuestionType.WICKETS_BY_BOWLER -> questionUpdated = handleWicketByBowlerQuestion(question, ballEvent)
                QuestionType.ECONOMY_RATE -> TODO()
                QuestionType.WIDES_IN_MATCH -> questionUpdated = handleWidesInMatchQuestion(question, ballEvent)
                QuestionType.TOTAL_EXTRAS -> questionUpdated = handleTotalExtrasQuestion(question, ballEvent)
                QuestionType.INVALID -> TODO()
            }

            if (questionUpdated) {
                questionRepository.save(question)
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

    private fun handleRunsInOversQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        val targetRuns = question.targetRuns!!
        val targetOvers = question.targetOvers!!

        if ((ballEvent.overNumber == targetOvers) && (ballEvent.ballNumber == 6)) {
            val totalRuns = calculateTotalRuns(ballEvent.inningId, targetOvers)
            if (totalRuns >= targetRuns) {
                return true
            } else {
                return false
            }
        }
        return false
    }

    private fun handleWicketByBowlerQuestion(
        question: QuestionDataModel,
        ballEvent: BallEvent
    ): Boolean {
        val playerId = question.playerId!!
        if (ballEvent.isWicket && ballEvent.bowlerId == playerId) {
            val wicketsTaken = calculateWicketsTaken(ballEvent.bowlerId, ballEvent.matchId)
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
                ballEvent.bowlerId,
                ballEvent.inningId,
                question.specificOver!!
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
        if (ballEvent.isWide &&
            ballEvent.bowlerId == question.playerId
        ) {
            val widesBowled = calculateWidesBowled(ballEvent.bowlerId, ballEvent.matchId)
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
        val totalExtras = calculateTotalExtras(ballEvent.inningId)
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
            val runsScored = calculateRunsScored(ballEvent.batsmanId, ballEvent.matchId)
            if (runsScored >= 100) {
                return true
            }
        }
        return false
    }

    private fun calculateTotalRuns(
        inningId: Int,
        upToOverNumber: Int
    ): Int {
        val scores = scoreRepository.findByInningIdAndOverNumberLessThanEqual(inningId, upToOverNumber)
        return scores.sumOf { it.runs }
    }

    private fun calculateWicketsTaken(bowlerId: Int, matchId: Int): Int {
        val performances = bowlerPerformanceRepository.findByPlayerIdAndMatchId(bowlerId, matchId)
        return performances.sumOf { it.wicketsTaken }
    }

    private fun calculateWicketsInOver(bowlerId: Int, inningId: Int, overNumber: Int): Int {
        val score = scoreRepository.findByInningIdAndOverNumber(inningId, overNumber)
        return score?.wickets ?: 0
    }

    private fun calculateWidesBowled(bowlerId: Int, matchId: Int): Int {
        val performances = bowlerPerformanceRepository.findByPlayerIdAndMatchId(bowlerId, matchId)
        return performances.sumOf { it.wides }
    }

    private fun calculateTotalExtras(inningId: Int): Int {
        val scores = scoreRepository.findByInningId(inningId)
        return scores.sumOf { it.wides + it.noBalls + it.byes + it.legByes + it.penaltyRuns }
    }

    private fun calculateRunsScored(batsmanId: Long, matchId: Int): Int {
        val performances = batsmanPerformanceRepository.findByPlayerIdAndMatchId(batsmanId, matchId)
        return performances.sumOf { it.runsScored }
    }
}

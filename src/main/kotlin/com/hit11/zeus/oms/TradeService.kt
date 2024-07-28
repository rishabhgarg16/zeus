package com.hit11.zeus.oms

import com.hit11.zeus.adapter.TradeAdapter.toTradeResponse
import com.hit11.zeus.repository.QuestionRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class TradeService(
    private val tradeRepository: TradeRepository,
    private val questionRepository: QuestionRepository
) {
    @Transactional
    fun createTrade(order: OrderDataModel, quantity: Long) {
        val trade = Trade(
            userId = order.userId,
            pulseId = order.pulseId,
            matchId = order.matchId,
            orderId = order.id,
            tradeQuantity = quantity,
            tradePrice = order.price,
            userAnswer = order.userAnswer,
            tradeAmount = order.price.multiply(quantity.toBigDecimal())
        )
        tradeRepository.save(trade)
    }

    fun getAllTradesByUserAndMatch(
        userId: Int,
        matchIdList: List<Int>
    ): List<TradeResponse>? {
        try {
            val allTrades = tradeRepository.findByUserIdAndMatchIdInOrderByCreatedAtDesc(userId, matchIdList)
            val matchQuestions = questionRepository.findAllByMatchIdIn(matchIdList).map { it.mapToQuestionDataModel() }
            val questionIdToQuestionMap = matchQuestions.associateBy { it.id }
            return allTrades.mapNotNull { trade ->
                questionIdToQuestionMap[trade.pulseId]?.let { trade.toTradeResponse(it) }
            }
        } catch (e: Exception) {
            throw e
        }
    }

    @Transactional
    fun updateTrade(trade: Trade) {
        tradeRepository.save(trade)
    }

    fun getTradesByQuestionId(questionId: Int): List<Trade>? {
        return tradeRepository.findByPulseId(questionId)
    }
}
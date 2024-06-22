package com.hit11.zeus.service

import com.hit11.zeus.adapter.UserPulseAdapter.addPulseData
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.PulseAnswerUpdateRequest
import com.hit11.zeus.model.PulseAnswerUpdateResponse
import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.TradeDataModel
import com.hit11.zeus.model.TradeResponse
import com.hit11.zeus.repository.PulseRepositorySql
import com.hit11.zeus.repository.UserRepository
import com.hit11.zeus.repository.UserRepositoryMysql
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class PulseService(
    private val orderService: OrderService,
    private val userRepository: UserRepository,
    private val userRepositoryMysql: UserRepositoryMysql,
    private val userService: UserService,
    private val pulseRepositorySql: PulseRepositorySql,
) {
    private val logger = Logger.getLogger(PulseService::class.java)
    fun getAllActiveOpinions(matchIdList: List<Int>): List<PulseDataModel>? {
        val activePulse = pulseRepositorySql.findByMatchIdInAndStatus(matchIdList, true)
        return activePulse?.map { it.mapToPulseDataModel() }
    }

    @Transactional
    fun submitTrade(response: TradeDataModel): TradeResponse? {
        // check pulse is not expired
//        val pulse = pulseRepositorySql.findByPulseId(response.pulseId)
        val amountToDeduct = "%.2f".format(response.tradeAmount).toDouble()
        try {
            val balanceSuccess = userService.updateBalance(response.userId, -amountToDeduct)

            if (balanceSuccess) {
                val userResponse = orderService.saveOrder(response)
                val pulseDoc = pulseRepositorySql.getPulseById(userResponse.pulseId).mapToPulseDataModel()
                return response.addPulseData(pulseDoc)
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
            val pulseData = pulseRepositorySql.findAllByMatchIdIn(matchIdList).map { it.mapToPulseDataModel() }
            val pulseMap: Map<Int, PulseDataModel> = pulseData.associateBy { it.id }.mapValues { it.value }
            return allTrades?.mapNotNull { trade -> pulseMap[trade.pulseId]?.let { trade.addPulseData(it) } }
        } catch (e: Exception) {
            throw e
        }
    }

    fun updatePulseAnswer(anserRequest: PulseAnswerUpdateRequest): PulseAnswerUpdateResponse {
        var res = PulseAnswerUpdateResponse()
        try {
            val pulse = pulseRepositorySql.getPulseById(anserRequest.pulseId)
            pulse.pulseResult = anserRequest.pulseResult
            pulse.status = false
            pulseRepositorySql.save(pulse)

            var orders = orderService.getAllTradesByPulseId(anserRequest.pulseId)
            orders?.forEach{
                if (it.userAnswer == anserRequest.pulseResult) {
                    it.userResult = "Win"
                    userService.updateBalance(it.userId, it.quantity*10.0)
                } else {
                    it.userResult = "Lose"
                }
                orderService.saveOrder(it)
            }
//            val success = repository.updatePulseAnswer(anserRequest.pulseId, anserRequest.pulseResult)
//            if (success) {
//                var usersToUpdate = userPulseRepository.updatePulseResultsForAllUsers(anserRequest.pulseId, anserRequest.pulseResult)
//                if (!usersToUpdate.isEmpty()) {
//                    res.updatedUserIds = usersToUpdate
//                    usersToUpdate.forEach{
//                        userRepository.updateBalance(it, 10.0)
//                    }
//                }
//            }
        } catch (e: Exception) {
            throw e
        }
        return res
    }

}

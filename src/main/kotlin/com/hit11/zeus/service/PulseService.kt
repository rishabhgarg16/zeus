package com.hit11.zeus.service

import com.hit11.zeus.adapter.UserPulseAdapter.toResponse
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.*
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class PulseService(
    private val orderService: OrderService,
    private val userRepository: UserRepository,
    private val pulseRepositorySql: PulseRepositorySql,
) {
    private val logger = Logger.getLogger(PulseService::class.java)
    fun getAllActiveOpinions(matchId: Int): List<PulseDataModel>? {
        val activePulse = pulseRepositorySql.findByMatchIdAndStatus(matchId, true)
        return activePulse?.map { it.mapToPulseDataModel() }
    }

    @Transactional
    fun submitUserTrade(response: UserPulseDataModel): UserPulseSubmissionResponse? {
        // check pulse is not expired
//        val pulse = pulseRepositorySql.findByPulseId(response.pulseId)
        val amountToDeduct = "%.2f".format(response.tradeAmount).toDouble()
        try {
            val balanceSuccess = userRepository.updateBalanceForUserRef(response.userId, -amountToDeduct)

            if (balanceSuccess) {
                val userResponse = orderService.saveOrder(response)
                val pulseDoc = pulseRepositorySql.getPulseById(userResponse.pulseId).mapToPulseDataModel()
                return response.toResponse(pulseDoc)
            } else {
                logger.error("Error updating the user wallet for user id ${response.userId}")
                throw RuntimeException("Error updating the user wallet for user id ${response.userId}")
            }
        } catch (e: Exception) {
            logger.error("Error in submitUserTrade for user id ${response.userId}", e)
            throw e
        }

    }

    fun getEnrolledPulsesByUser(userId: Int, matchIdList: List<Int>): List<UserPulseSubmissionResponse>? {
        try {
            val userResponse = orderService.getOrdersByUserIdAndMatchIdIn(userId, matchIdList)
            val pulseData = pulseRepositorySql.findAllByMatchIdIn(matchIdList).map { it.mapToPulseDataModel() }
            val pulseMap: Map<Int, PulseDataModel> = pulseData.associateBy { it.id }.mapValues { it.value }
            return userResponse?.mapNotNull { order -> pulseMap[order.pulseId]?.let { order.toResponse(it) } }
        } catch (e: Exception) {
            throw e
        }
    }

//    fun updatePulseAnswer(anserRequest: PulseAnswerUpdateRequest): PulseAnswerUpdateResponse {
//        var res = PulseAnswerUpdateResponse()
//        try {
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
//        } catch (e: Exception) {
//            throw e
//        }
//        return res
//    }

}

package com.hit11.zeus.service

import com.hit11.zeus.model.*
import com.hit11.zeus.repository.PulseRepository
import com.hit11.zeus.repository.UserPulseRepository
import com.hit11.zeus.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class PulseService(
    private val repository: PulseRepository,
    private val userPulseRepository: UserPulseRepository,
    private val userRepository: UserRepository,
) {

    fun getAllActiveOpinions(matchId: String): List<PulseDataModel>? =
        repository.getAllActivePulseByMatch(matchId)

    fun submitResponse(response: UserPulseDataModel): UserPulseSubmissionResponse? {
        try {
            val userResponse = repository.saveUserResponse(response)
            val balanceSuccess = userRepository.updateBalance(response.userId, -response.userWager)
            val pulseDoc = repository.getPulseById(userResponse?.pulseId)
            return response.toResponse(pulseDoc)
        } catch (e: Exception) {
            throw e
        }
    }

    fun getEnrolledPulsesByUser(userId: String): List<UserPulseSubmissionResponse>? {

        try {
            val userResponse = repository.getEnrolledPulsesByUser(userId)
            return userResponse!!.map {
                val pulseDoc = repository.getPulseById(it.pulseId)
                it.toResponse(pulseDoc)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun getEnrolledPulsesByUserAndMatch(userId: String, matchIdRef: String): List<UserPulseSubmissionResponse>? {

        try {
            val userResponse = repository.getEnrolledPulsesByUserAndMatch(userId, matchIdRef)
            return userResponse!!.map {
                val pulseDoc = repository.getPulseById(it.pulseId)
                it.toResponse(pulseDoc)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    fun updatePulseAnswer(anserRequest: PulseAnswerUpdateRequest): PulseAnswerUpdateResponse {
        var res = PulseAnswerUpdateResponse()
        try {
            val success = repository.updatePulseAnswer(anserRequest.pulseId, anserRequest.pulseResult)
            if (success) {
                var usersToUpdate = userPulseRepository.updatePulseResultsForAllUsers(anserRequest.pulseId, anserRequest.pulseResult)
                if (!usersToUpdate.isEmpty()) {
                    res.updatedUserIds = usersToUpdate
                    usersToUpdate.forEach{
                        userRepository.updateBalance(it, 10.0)
                    }
                }
            }
        } catch (e: Exception) {
            throw e
        }
        return res
    }
}

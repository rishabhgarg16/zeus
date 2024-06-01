package com.hit11.zeus.service

import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.UserPulseDataModel
import com.hit11.zeus.repository.OpinionRepository
import org.springframework.stereotype.Service

@Service
class OpinionService(private val repository: OpinionRepository) {

    fun getAllActiveOpinions(matchId: String): List<PulseDataModel>? =
        repository.getAllActiveOpinionsByMatch(matchId.toInt())

    fun submitResponse(response: UserPulseDataModel): UserPulseDataModel? = repository.saveUserResponse(response)

    fun getEnrolledPulsesByUser(userId: Int): List<UserPulseDataModel>? = repository.getEnrolledPulsesByUser(userId)

    fun getEnrolledPulsesByUserAndMatch(userId: Int, matchId: Int): List<UserPulseDataModel>? =
        repository.getEnrolledPulsesByUserAndMatch(userId, matchId)
}

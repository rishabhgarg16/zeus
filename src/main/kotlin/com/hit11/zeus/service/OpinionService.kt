package com.hit11.zeus.service

import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.UserPulseDataModel
import com.hit11.zeus.repository.PulseRepository
import org.springframework.stereotype.Service

@Service
class OpinionService(private val repository: PulseRepository) {

    fun getAllActiveOpinions(matchId: String): List<PulseDataModel>? =
        repository.getAllActivePulseByMatch(matchId)

    fun submitResponse(response: UserPulseDataModel): UserPulseDataModel? = repository.saveUserResponse(response)

    fun getEnrolledPulsesByUser(userId: Int): List<UserPulseDataModel>? = repository.getEnrolledPulsesByUser(userId)

    fun getEnrolledPulsesByUserAndMatch(userId: Int, matchId: Int): List<UserPulseDataModel>? =
        repository.getEnrolledPulsesByUserAndMatch(userId, matchId)
}

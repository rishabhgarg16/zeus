package com.hit11.zeus.service

import com.hit11.zeus.model.Contest
import com.hit11.zeus.repository.ContestRepository
import org.springframework.stereotype.Service

@Service
class ContestService(
    private val contestRepository: ContestRepository
) {
    fun getContestForMatch(matchId: Int): List<Contest> {
        return contestRepository.getContestForMatch(matchId)
    }
}

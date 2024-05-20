package com.hit11.citus.service

import com.hit11.citus.model.Match
import com.hit11.citus.repository.MatchRepository
import org.springframework.stereotype.Service

@Service
class MatchService(private val matchRepository: MatchRepository) {
    fun getUpcomingMatches(): List<Match> {
        return matchRepository.getUpcomingMatches()
    }
}

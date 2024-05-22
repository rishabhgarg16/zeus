package com.hit11.zeus.service

import com.hit11.zeus.model.Match
import com.hit11.zeus.repository.MatchRepository
import org.springframework.stereotype.Service

@Service
class MatchService(private val matchRepository: MatchRepository) {
    fun getUpcomingMatches(): List<Match> {
        return matchRepository.getUpcomingMatches()
    }
}

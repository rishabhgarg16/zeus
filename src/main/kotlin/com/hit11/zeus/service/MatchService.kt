package com.hit11.zeus.service

import com.hit11.zeus.model.Match
import com.hit11.zeus.model.MatchStatus
import com.hit11.zeus.repository.MatchRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class MatchService(
    private val matchRepository: MatchRepository,
) {
    fun getUpcomingMatches(limit: Int): List<Match> {
        val activeStatuses =
            listOf(MatchStatus.SCHEDULED.text, MatchStatus.IN_PROGRESS.text, MatchStatus.PREVIEW.text)
        val startDate = Instant.now().minus(1, ChronoUnit.DAYS)
        val pageable = PageRequest.of(0, limit)

        return try {
            matchRepository.findMatchesWithTeams(activeStatuses, startDate, pageable)
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
            emptyList()
        }
    }

    fun getMatchById(matchId: Int): Match? {
        return try {
            matchRepository.findMatchWithTeamsById(matchId)
        } catch (e: Exception) {
            println("Error fetching match by ID: $e")
            null
        }
    }
}

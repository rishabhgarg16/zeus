package com.hit11.zeus.service

import com.hit11.zeus.model.Match
import com.hit11.zeus.model.MatchStatus
import com.hit11.zeus.repository.MatchRepository
import com.hit11.zeus.repository.TeamRepository
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
            val matchEntities = matchRepository.findMatchesWithTeams(activeStatuses, startDate, pageable)
            matchEntities.mapNotNull { matchEntity ->
                try {
                    matchEntity.mapToMatch()
                } catch (e: Exception) {
                    println("Error deserializing match : ${matchEntity.id} $e")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
            emptyList()
        }
    }

    fun getMatchById(matchId: Int): Match? {
        return try {
            val matchEntity = matchRepository.findById(matchId)
            if (!matchEntity.isPresent) {
                return null
            }
            matchEntity.get().mapToMatch()
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
            null
        }
    }
}

package com.hit11.zeus.service

import com.hit11.zeus.model.Match
import com.hit11.zeus.model.MatchEntity
import com.hit11.zeus.repository.MatchRepository
import com.hit11.zeus.repository.TeamRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant

@Service class MatchService(
    private val matchRepository: MatchRepository,
    private val teamRepository: TeamRepository
) {
    fun getUpcomingMatches(limit: Int): List<Match> {
        val now = Instant.now()
        val pageable: Pageable = PageRequest.of(
            0,
            limit
        )
        return try {
            val matchEntities: List<MatchEntity> =
                    matchRepository.findMatchesWithLimit(
                        now,
                        pageable
                    )
            matchEntities.mapNotNull { matchEntity ->
                try {
                    val team1 = teamRepository.findById(matchEntity.team1Id.toLong()).orElse(null)
                    val team2 = teamRepository.findById(matchEntity.team2Id.toLong()).orElse(null)

                    matchEntity.mapToMatch(team1, team2)
                } catch (e: Exception) {
                    println("Error deserializing match : ${matchEntity.id} $e")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
            emptyList<Match>()
        }
    }
}

package com.hit11.zeus.service

import com.hit11.zeus.model.Match
import com.hit11.zeus.model.MatchEntity
import com.hit11.zeus.repository.MatchRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant

@Service class MatchService(
    private val matchRepository: MatchRepository
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
                    matchEntity.mapToMatch()
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

package com.hit11.zeus.service

import com.hit11.zeus.model.Match
import com.hit11.zeus.model.MatchEntity
import com.hit11.zeus.model.mapToMatch
import com.hit11.zeus.repository.MatchRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class MatchService(
    private val matchRepository: MatchRepository
) {
    fun getUpcomingMatches(limit: Int): List<Match> {
        val matches: MutableList<Match> = mutableListOf()
        matches.clear()

        val now = Instant.now()
        val pageable: Pageable = PageRequest.of(0, limit)
        try {
            val matchEntities: List<MatchEntity> =
                    matchRepository.findMatchesWithLimit(now, pageable)
            for (matchEntity in matchEntities) {
                try {
                    val match: Match = mapToMatch(matchEntity)
                    matches.add(match)
                } catch (e: Exception) {
                    println("Error deserializing match : ${matchEntity.id} $e")
                }
            }
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
        }

        return matches
    }
}

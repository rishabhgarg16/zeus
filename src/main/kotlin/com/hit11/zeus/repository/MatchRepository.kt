package com.hit11.zeus.repository

import com.hit11.zeus.model.MatchEntity
import com.hit11.zeus.model.MatchStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.*

@Repository interface MatchRepository : JpaRepository<MatchEntity, Int> {
    @Query("""SELECT m FROM MatchEntity m
    JOIN FETCH m.team1 t1
    JOIN FETCH m.team2 t2
    WHERE m.status IN :statuses
    AND m.startDate >= :startDate
    ORDER BY m.startDate ASC""")
    fun findMatchesWithTeams(statuses: List<String>, startDate: Instant, pageable: Pageable): List<MatchEntity>

    @Query("""
    SELECT m FROM MatchEntity m
    JOIN FETCH m.team1
    JOIN FETCH m.team2
    WHERE m.id = :matchId
    """)
    fun findMatchWithTeamsById(@Param("matchId") matchId: Int): Optional<MatchEntity>

}

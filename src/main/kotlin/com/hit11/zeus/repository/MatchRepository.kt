package com.hit11.zeus.repository

import com.hit11.zeus.model.Match
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface MatchRepository : JpaRepository<Match, Int> {
    @Query(
        """  
    SELECT m FROM Match m 
    JOIN FETCH m.team1 t1 
    JOIN FETCH m.team2 t2 
    WHERE (
        m.status IN :activeStatuses 
        OR (
            m.startDate <= :currentTimestamp 
            AND m.endDate >= :currentTimestamp
        )
        OR (
            m.status = 'Complete' 
            AND m.endDate >= :recentCompletedMatchThreshold
        )
    )
    ORDER BY 
        CASE 
            WHEN m.status = 'In Progress' THEN 0 
            WHEN m.status = 'Preview' THEN 1 
            WHEN m.status = 'Scheduled' THEN 2
            WHEN m.status = 'Complete' THEN 3
        END, 
        m.startDate ASC
    """
    )
    fun findMatchesWithTeams(
        activeStatuses: List<String>,
        currentTimestamp: Instant,
        recentCompletedMatchThreshold: Instant,
        pageable: Pageable
    ): List<Match>

    fun findAllByIdInAndStatusIn(ids: List<Int>, statuses: List<String>): List<Match>

    @Query(
        """
    SELECT m FROM Match m
    JOIN FETCH m.team1
    JOIN FETCH m.team2
    WHERE m.id = :matchId
    """
    )
    fun findMatchWithTeamsById(@Param("matchId") matchId: Int): Match?

    @Query(
        """
    SELECT m FROM Match m
    WHERE m.id = :matchId
    AND m.status IN ('Scheduled', 'Preview', 'In Progress')
    """
    )
    fun findActiveMatchById(@Param("matchId") matchId: Int): Optional<Match>

}

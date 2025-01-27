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
    SELECT DISTINCT m FROM Match m 
    JOIN FETCH m.team1Entity t1 
    JOIN FETCH m.team2Entity t2 
    WHERE (
        (m.status IN :activeStatuses AND m.endDate >= :currentTimestamp) 
        OR (
            m.status = 'Complete' 
            AND m.endDate >= :recentCompletedMatchThreshold
        )
    )
    ORDER BY 
        CASE m.status
            WHEN 'In Progress' THEN 0 
            WHEN 'Preview' THEN 1 
            WHEN 'Scheduled' THEN 2
            ELSE 3 
        END,
        CASE 
            WHEN m.status = 'Complete' THEN m.endDate 
            ELSE m.startDate 
        END ASC
    """
    )
    fun findMatchesWithTeams(
        activeStatuses: List<String>,
        currentTimestamp: Instant,
        recentCompletedMatchThreshold: Instant,
        pageable: Pageable
    ): List<Match>

    fun findByCricbuzzMatchId(cricbuzzMatchId: Int): Match?

    fun findAllByIdInAndStatusIn(ids: List<Int>, statuses: List<String>): List<Match>

    @Query(
        """
    SELECT m FROM Match m
    JOIN FETCH m.team1Entity
    JOIN FETCH m.team2Entity
    WHERE m.id = :matchId
    """
    )
    fun findMatchWithTeamsById(@Param("matchId") matchId: Int): Match?

    @Query(
        """
    SELECT m FROM Match m
    WHERE m.id = :matchId
    AND m.status NOT IN ('Complete')
    """
    )
    fun findActiveMatchById(@Param("matchId") matchId: Int): Optional<Match>

}

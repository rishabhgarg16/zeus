package com.hit11.zeus.repository

import com.hit11.zeus.model.MatchEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Pageable
import java.time.Instant

@Repository interface MatchRepository : JpaRepository<MatchEntity, Int> {
    @Query("SELECT m FROM MatchEntity m WHERE m.startDate > :startDate ORDER BY m.startDate ASC")
    fun findMatchesWithLimit(
        @Param("startDate") startDate: Instant,
        pageable: Pageable
    ): List<MatchEntity>
}

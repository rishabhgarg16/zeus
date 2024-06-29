package com.hit11.zeus.repository

import com.hit11.zeus.model.MatchEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Pageable
import java.time.Instant

@Repository interface MatchRepository : JpaRepository<MatchEntity, Int> {
    @Query("SELECT m FROM MatchEntity m WHERE m.endDate > :startDate ORDER BY m.startDate ASC")
    fun findMatchesWithLimit(
        @Param("startDate") startDate: Instant,
        pageable: Pageable
    ): List<MatchEntity>

    @Query("SELECT m FROM MatchEntity m WHERE m.status = :status ORDER BY m.startDate ASC")
    fun findMatchesWithLiveStatusWithLimit(
        status: String,
        pageable: Pageable
    ): List<MatchEntity>
}

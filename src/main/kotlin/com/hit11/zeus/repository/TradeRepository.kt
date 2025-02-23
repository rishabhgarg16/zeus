package com.hit11.zeus.repository

import com.hit11.zeus.model.Trade
import com.hit11.zeus.model.TradeStatus
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.domain.Pageable
import java.time.Instant

interface TradeRepository : JpaRepository<Trade, Long> {
    fun findByPulseId(pulseId: Int): List<Trade>
    fun findByOrderId(order: Long): List<Trade>
    fun findByUserId(userId: Int): List<Trade>
    fun findByMatchId(matchId: Int): List<Trade>
    fun findByPulseIdAndStatus(pulseId: Int, status: TradeStatus): List<Trade>
    fun findByPulseIdAndCreatedAtBetween(pulseId: Int, startDate: Instant, endDate: Instant): List<Trade>
    /**
     * Fetch live trades (active).
     */
    fun findByUserIdAndStatus(userId: Int, status: TradeStatus, pageable: Pageable): List<Trade>

    /**
     * Fetch completed trades with pagination.
     */
    fun findByUserIdAndStatusIn(userId: Int, statusList: List<TradeStatus>, pageable: Pageable): Page<Trade>

    fun findByUserIdAndMatchIdIn(pulseId: Int, matchIdList: List<Int>, pageable: Pageable): Page<Trade>
    @Query(
        value = "SELECT * FROM trades WHERE pulse_id = :pulseId ORDER BY created_at DESC LIMIT :limit",
        nativeQuery = true
    )
    fun findTopByPulseIdOrderByCreatedAtDesc(
        @Param("pulseId") pulseId: Int, @Param("limit") limit: Int
    ): List<Trade>?
}
package com.hit11.zeus.repository

import com.hit11.zeus.model.Trade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant


@Repository
interface TradeRepository : JpaRepository<Trade, Long> {
    fun findByPulseId(pulseId: Int): List<Trade>
    fun findByYesOrderIdOrNoOrderId(yesOrderId: Long, noOrderId: Long): List<Trade>
    fun findByMatchId(matchId: Int): List<Trade>
    fun findByPulseIdAndCreatedAtBetween(pulseId: Int, startDate: Instant, endDate: Instant): List<Trade>
    @Query(
        value = "SELECT * FROM trades WHERE pulse_id = :pulseId ORDER BY created_at DESC LIMIT :limit",
        nativeQuery = true
    )
    fun findTopByPulseIdOrderByCreatedAtDesc(@Param("pulseId") pulseId: Int, @Param("limit") limit: Int): List<Trade>?
}
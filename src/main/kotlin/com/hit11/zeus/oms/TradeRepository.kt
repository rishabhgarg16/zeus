package com.hit11.zeus.oms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TradeRepository : JpaRepository<Trade, Long> {
    fun findByUserIdAndPulseIdIn(userId: Int, pulseIds: List<Int>): List<Trade>
    fun findByUserIdAndMatchIdIn(userId: Int, pulseIds: List<Int>): List<Trade>
    fun findByPulseId(pulseId: Int): List<Trade>
}
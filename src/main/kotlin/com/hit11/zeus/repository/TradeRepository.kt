package com.hit11.zeus.repository

import com.hit11.zeus.model.Trade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TradeRepository : JpaRepository<Trade, Long> {
    fun findByPulseId(pulseId: Int): List<Trade>
    fun findByBuyOrderIdOrSellOrderId(buyOrderId: Long, sellOrderId: Long): List<Trade>
    fun findByMatchId(matchId: Int): List<Trade>
//    fun findByUserIdAndPulseIdIn(userId: Int, pulseIds: List<Int>): List<Trade>
//    fun findByUserIdAndMatchIdInOrderByCreatedAtDesc(userId: Int, pulseIds: List<Int>): List<Trade>
//    fun findByPulseId(pulseId: Int): List<Trade>
}
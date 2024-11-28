package com.hit11.zeus.repository

import com.hit11.zeus.model.MatchedOrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface MatchedOrderRepository : JpaRepository<MatchedOrderEntity, Long> {
    fun findByPulseId(pulseId: Int): List<MatchedOrderEntity>
    fun findByMatchId(matchId: Int): List<MatchedOrderEntity>
    fun findByYesOrderIdOrNoOrderId(yesOrderId: Long, noOrderId: Long): List<MatchedOrderEntity>
}



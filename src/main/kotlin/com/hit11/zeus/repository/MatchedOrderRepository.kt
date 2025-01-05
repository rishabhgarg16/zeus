package com.hit11.zeus.repository

import com.hit11.zeus.model.OrderExecution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface MatchedOrderRepository : JpaRepository<OrderExecution, Long> {
    fun findByPulseId(pulseId: Int): List<OrderExecution>
    fun findByMatchId(matchId: Int): List<OrderExecution>
    fun findByYesOrderIdOrNoOrderId(yesOrderId: Long, noOrderId: Long): List<OrderExecution>
    fun findTopByPulseIdOrderByCreatedAtDesc(pulseId: Int): OrderExecution?
}



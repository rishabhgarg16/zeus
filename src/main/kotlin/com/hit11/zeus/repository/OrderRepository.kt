package com.hit11.zeus.repository

import com.hit11.zeus.model.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface OrderRepository : JpaRepository<OrderEntity, Int> {
    fun findTradesByUserIdAndMatchIdIn(userId: Int, matchId: List<Int>): List<OrderEntity>?
    fun findOrderByUserIdAndMatchIdAndPulseId(userId: Int, matchId: Int, pulseId: Int): OrderEntity?
    fun findOrderByUserId(userId: Int): List<OrderEntity>
    fun findTradesByPulseId(pulseId: Int): List<OrderEntity>
}
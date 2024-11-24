package com.hit11.zeus.repository

import com.hit11.zeus.model.Order
import com.hit11.zeus.model.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface OrderRepository : JpaRepository<Order, Int> {
//    fun findTradesByUserIdAndMatchIdIn(userId: Int, matchId: List<Int>): List<Order>?
//    fun findOrderByUserIdAndMatchIdAndPulseId(userId: Int, matchId: Int, pulseId: Int): Order?
    fun findByUserIdAndStatus(userId: Int, status: OrderStatus): List<Order>
    fun findByPulseIdAndStatus(pulseId: Int, status: OrderStatus): List<Order>
    fun findAllByStatusIn(statusList: List<OrderStatus>): List<Order>
//    fun findOrderByUserId(userId: Int): List<Order>
//    fun findTradesByPulseId(pulseId: Int): List<Order>


}
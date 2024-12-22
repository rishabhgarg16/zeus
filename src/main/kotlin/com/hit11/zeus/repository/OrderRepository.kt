package com.hit11.zeus.repository

import com.hit11.zeus.model.Order
import com.hit11.zeus.model.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface OrderRepository : JpaRepository<Order, Long> {

    fun findByUserIdAndStatus(userId: Int, status: OrderStatus): List<Order>
    fun findByPulseIdAndStatus(
        pulseId: Int,
        status: OrderStatus
    ): List<Order>

    fun findByPulseIdAndStatusIn(
        pulseId: Int,
        status: List<OrderStatus>
    ): List<Order>


    fun findByUserIdAndPulseIdAndStatusIn(
        userId: Int,
        pulseId: Int,
        status: List<OrderStatus>
    ): List<Order>

    fun findByUserIdAndMatchIdInAndStatusIn(
        userId: Int,
        matchIds: List<Int>,
        statuses: List<OrderStatus>
    ): List<Order>

    fun findByUserIdAndPulseIdAndStatus(
        userId: Int,
        pulseId: Int,
        status: OrderStatus
    ): List<Order>

    fun findAllByStatusIn(statusList: List<OrderStatus>): List<Order>
}
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
//    fun findOrderByUserId(userId: Int): List<Order>
//    fun findTradesByPulseId(pulseId: Int): List<Order>

//    @Query("SELECT o FROM OrderEntity o " +
//            "WHERE o.pulseId = :pulseId " +
//            "AND o.userAnswer != :userAnswer " +
//            "AND o.price = :price " +
//            "AND o.state = :state " +
//            "AND o.remainingQuantity >= :quantity " +
//            "ORDER BY o.createdAt ASC")
//    fun findMatchingOrder(
//        pulseId: Int,
//        userAnswer: String,
//        price: BigDecimal,
//        state: OrderStatus,
//        quantity: Long
//    ): List<Order>?
//    fun findByUserIdAndPulseIdAndUserAnswer(userId: Int, pulseId: Int, userAnswer: String): List<Order>?
}
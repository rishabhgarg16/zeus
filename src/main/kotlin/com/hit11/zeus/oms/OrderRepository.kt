package com.hit11.zeus.oms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface OrderRepository : JpaRepository<OrderEntity, Int> {
    fun findTradesByUserIdAndMatchIdIn(userId: Int, matchId: List<Int>): List<OrderEntity>?

    fun findOrderByUserIdAndMatchIdAndPulseId(userId: Int, matchId: Int, pulseId: Int): OrderEntity?

    fun findOrderByUserId(userId: Int): List<OrderEntity>

    fun findTradesByPulseId(pulseId: Int): List<OrderEntity>

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.pulseId = :pulseId " +
            "AND o.userAnswer != :userAnswer " +
            "AND o.price = :price " +
            "AND o.state = :state " +
            "AND o.remainingQuantity >= :quantity " +
            "ORDER BY o.createdAt ASC")
    fun findMatchingOrder(
        pulseId: Int,
        userAnswer: String,
        price: BigDecimal,
        state: OrderState,
        quantity: Long
    ): List<OrderEntity>?

    fun findByPulseIdAndState(pulseId: Int, state: OrderState): List<OrderEntity>
}
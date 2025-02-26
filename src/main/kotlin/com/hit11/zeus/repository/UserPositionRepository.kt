package com.hit11.zeus.repository

import com.hit11.zeus.model.OrderSide
import com.hit11.zeus.model.PositionStatus
import com.hit11.zeus.model.UserPosition
import org.springframework.data.jpa.repository.JpaRepository

interface UserPositionRepository : JpaRepository<UserPosition, Long> {
    fun findByUserIdAndPulseId(userId: Int, pulseId: Int): List<UserPosition>?
    fun findByPulseIdAndStatus(pulseId: Int, status: PositionStatus): List<UserPosition>
    fun findByUserIdAndPulseIdAndOrderSide(userId: Int, pulseId: Int, side: OrderSide): UserPosition?
    fun findAllByUserIdAndPulseIdAndOrderSideAndStatus(
        userId: Int,
        pulseId: Int,
        side: OrderSide,
        status: PositionStatus
    ): List<UserPosition>

    fun findByPulseId(pulseId: Int): List<UserPosition>
    fun findByUserId(userId: Int): List<UserPosition>
}
package com.hit11.zeus.repository

import com.hit11.zeus.model.PositionStatus
import com.hit11.zeus.model.Trade
import com.hit11.zeus.model.UserPosition
import org.springframework.data.jpa.repository.JpaRepository

interface UserPositionRepository : JpaRepository<UserPosition, Long> {
    fun findByUserIdAndPulseId(userId: Int, pulseId: Int): UserPosition?
    fun findByPulseId(pulseId: Int): List<UserPosition>
    fun findByUserId(userId: Int): List<UserPosition>
    fun findByPulseIdAndStatus(pulseId: Int, status: PositionStatus): List<UserPosition>
}
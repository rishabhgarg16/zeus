package com.hit11.zeus.repository

import com.hit11.zeus.model.PulseQuestionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface PulseRepositorySql : JpaRepository<PulseQuestionEntity, Int> {
    fun findByMatchIdInAndStatus(matchIdList: List<Int>, status: Boolean): List<PulseQuestionEntity>?
    fun findByMatchId(matchId: Int): List<PulseQuestionEntity>?
    fun findAllByStatus(status: Boolean): List<PulseQuestionEntity>
    fun findAllByMatchIdIn(matchIds: List<Int>): List<PulseQuestionEntity>
    fun getPulseById(id: Int): PulseQuestionEntity
}
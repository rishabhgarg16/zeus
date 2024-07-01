package com.hit11.zeus.repository

import com.hit11.zeus.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface QuestionRepository : JpaRepository<QuestionEntity, Int> {
    fun findByMatchIdInAndStatus(matchIdList: List<Int>, status: Boolean): List<QuestionEntity>?
    fun findByMatchId(matchId: Int): List<QuestionEntity>?
    fun findAllByStatus(status: Boolean): List<QuestionEntity>
    fun findAllByMatchIdIn(matchIds: List<Int>): List<QuestionEntity>
    fun getPulseById(id: Int): QuestionEntity
}

interface InningRepository : JpaRepository<Inning, Int>

interface ScoreRepository : JpaRepository<Score, Int> {
    fun findByInningIdAndOverNumber(inningId: Int, overNumber: Int): Score?
}

interface BatsmanPerformanceRepository : JpaRepository<BatsmanPerformance, Int> {
    fun findByInningIdAndPlayerId(inningId: Int, playerId: Int): List<BatsmanPerformance>
}

interface BowlerPerformanceRepository : JpaRepository<BowlerPerformance, Int> {
    fun findByInningIdAndPlayerId(inningId: Int, playerId: Int): List<BowlerPerformance>
}

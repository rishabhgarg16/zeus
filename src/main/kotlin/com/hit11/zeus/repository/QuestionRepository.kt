package com.hit11.zeus.repository

import com.hit11.zeus.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository interface QuestionRepository : JpaRepository<QuestionEntity, Int> {
    fun findByMatchIdInAndStatus(
        matchIdList: List<Int>,
        status: Boolean
    ): List<QuestionEntity>?

    fun findByMatchId(matchId: Int): List<QuestionEntity>?
    fun findAllByStatus(status: Boolean): List<QuestionEntity>
    fun findAllByMatchIdIn(matchIds: List<Int>): List<QuestionEntity>
    fun getPulseById(id: Int): QuestionEntity
}

interface InningRepository : JpaRepository<Inning, Int> {
    fun findByMatchIdAndInningNumber(matchId: Int, inningNumber: Int): Inning?
}

interface ScoreRepository : JpaRepository<Score, Int> {

    @Query(
        "SELECT COUNT() FROM ScoreEntity " +
        "WHERE matchId = :matchId " +
        "AND bowlerId = :bowlerId " +
        "AND overNumber = :overNumber " +
        "AND isWicket = true " +
        "GROUP BY matchId, bowlerId, overNumber"
    )
    fun findWicketsByOverNumber(
        matchId: Int,
        bowlerId: Int,
        overNumber: Int
    ): Int

    fun findTopByMatchIdAndInningIdOrderByOverNumberDescBallNumberDesc(
        matchId: Int,
        inningId: Int
    ): Score?

    @Query("SELECT * FROM ScoreEntity WHERE matchId = :matchId AND inningId = :inningId")
    fun findByMatchIdAndInningId(
        matchId: Int,
        inningId: Int
    ): List<Score>?
}

interface BatsmanPerformanceRepository : JpaRepository<BatsmanPerformance, Int> {
    fun findByMatchIdIdAndPlayerId(
        matchId: Int,
        playerId: Int
    ): BatsmanPerformance?
}

interface BowlerPerformanceRepository : JpaRepository<BowlerPerformance, Int> {
    fun findByMatchIdIdAndPlayerId(
        matchId: Int,
        playerId: Int
    ): BowlerPerformance?
}

interface BallEventRepository : JpaRepository<BallEvent, Int> {

    fun findTopByInningIdOrderByOverNumberDescBallNumberDesc(inningId: Int): BallEvent?
}

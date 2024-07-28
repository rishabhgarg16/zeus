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

@Repository interface InningRepository : JpaRepository<Inning, Int> {
    fun findByMatchIdAndInningsNumber(matchId: Int, inningsNumber: Int): Inning?
}

@Repository interface BallEventRepository : JpaRepository<BallEventEntity, Int> {

    @Query(
        "SELECT COUNT(*) FROM BallEventEntity " +
        "WHERE matchId = :matchId " +
        "AND bowlerId = :bowlerId " +
        "AND overNumber = :overNumber " +
        "AND isWicket = true "
    )
    fun findWicketsByMatchIdAndBowlerIdAndOverNumber(
        matchId: Int,
        bowlerId: Int,
        overNumber: Int
    ): Int

    fun findTopByMatchIdAndInningIdOrderByOverNumberDescBallNumberDesc(
        matchId: Int,
        inningId: Int
    ): BallEventEntity?

    @Query("SELECT s FROM BallEventEntity s WHERE s.matchId = :matchId AND s.inningId = :inningId")
    fun findByMatchIdAndInningId(
        matchId: Int,
        inningId: Int
    ): List<BallEventEntity>?
}

@Repository interface BatsmanPerformanceRepository : JpaRepository<BatsmanPerformance, Int> {
    fun findByMatchIdAndPlayerId(
        matchId: Int,
        playerId: Int
    ): BatsmanPerformance?

    fun findByMatchIdAndPlayerIdIn(
        matchId: Int,
        playerIds: List<Int>
    ): List<BatsmanPerformance?>
}

@Repository interface BowlerPerformanceRepository : JpaRepository<BowlerPerformance, Int> {
    fun findByMatchIdAndPlayerId(
        matchId: Int,
        playerId: Int
    ): BowlerPerformance?

    fun findByMatchIdAndPlayerIdIn(
        matchId: Int,
        playerIds: List<Int>
    ) : List<BowlerPerformance?>
}
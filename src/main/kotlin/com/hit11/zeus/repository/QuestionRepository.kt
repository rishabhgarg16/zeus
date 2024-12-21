package com.hit11.zeus.repository

import com.hit11.zeus.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuestionRepository : JpaRepository<QuestionEntity, Int> {
    fun findByMatchIdInAndStatus(
        matchIdList: List<Int>,
        status: QuestionStatus
    ): List<QuestionEntity>?

    fun findByMatchIdAndStatusIn(
        matchIdList: Int,
        status: List<QuestionStatus>
    ): List<QuestionEntity>?

    // Match Winner
    fun existsByMatchIdAndQuestionTypeAndTargetTeamId(
        matchId: Int,
        questionType: QuestionType,
        targetTeamId: Int
    ): Boolean

    // Wickets by Bowler
    fun existsByMatchIdAndQuestionTypeAndTargetBowlerIdAndTargetWickets(
        matchId: Int,
        questionType: QuestionType,
        targetBowlerId: Int,
        targetWickets: Int
    ): Boolean

    // Runs Scored by Batsman
    fun existsByMatchIdAndQuestionTypeAndTargetBatsmanIdAndTargetRuns(
        matchId: Int,
        questionType: QuestionType,
        targetBatsmanId: Int,
        targetRuns: Int
    ): Boolean

    // Team Runs in Match
    fun existsByMatchIdAndQuestionTypeAndTargetTeamIdAndTargetRuns(
        matchId: Int,
        questionType: QuestionType,
        targetTeamId: Int,
        targetRuns: Int
    ): Boolean

    // Sixes by Player
    fun existsByMatchIdAndQuestionTypeAndTargetBatsmanIdAndTargetSixes(
        matchId: Int,
        questionType: QuestionType,
        targetBatsmanId: Int,
        targetSixes: Int
    ): Boolean

    // Toss Winner
    fun existsByMatchIdAndQuestionType(
        matchId: Int,
        questionType: QuestionType
    ): Boolean

    // Toss Decision
    fun existsByMatchIdAndQuestionTypeAndTargetTossDecision(
        matchId: Int,
        questionType: QuestionType,
        targetTossDecision: String
    ): Boolean

    // Wickets in Over
    fun existsByMatchIdAndQuestionTypeAndTargetBowlerIdAndTargetWicketsAndTargetSpecificOver(
        matchId: Int,
        questionType: QuestionType,
        targetBowlerId: Int,
        targetWickets: Int,
        targetSpecificOver: Int
    ): Boolean

    fun existsByMatchIdAndQuestionTypeAndTargetTeamIdAndTargetExtras(
        matchId: Int,
        questionType: QuestionType,
        targetTeamId: Int,
        targetExtras: Int
    ): Boolean

    fun existsByMatchIdAndQuestionTypeAndTargetBowlerIdAndTargetWides(
        matchId: Int,
        questionString: QuestionType,
        targetBowlerId: Int,
        targetWides: Int
    ): Boolean

    fun existsByMatchIdAndQuestionTypeAndTargetBatsmanId(
        matchId: Int,
        questionType: QuestionType,
        targetBatsmanId: Int
    ): Boolean

    fun findByMatchId(matchId: Int): List<QuestionEntity>?
    fun findAllByStatus(status: QuestionStatus): List<QuestionEntity>
    fun findAllByMatchIdIn(matchIds: List<Int>): List<QuestionEntity>
    fun getPulseById(id: Int): QuestionEntity
}

@Repository
interface InningRepository : JpaRepository<Inning, Int> {
    fun findByMatchIdAndInningsNumber(matchId: Int, inningsNumber: Int): Inning?
}

@Repository
interface BallEventRepository : JpaRepository<BallEventEntity, Int> {

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

@Repository
interface BatsmanPerformanceRepository : JpaRepository<BatsmanPerformance, Int> {
    fun findByMatchIdAndPlayerId(
        matchId: Int,
        playerId: Int
    ): BatsmanPerformance?

    fun findByMatchIdAndPlayerIdIn(
        matchId: Int,
        playerIds: List<Int>
    ): List<BatsmanPerformance?>
}

@Repository
interface BowlerPerformanceRepository : JpaRepository<BowlerPerformance, Int> {
    fun findByMatchIdAndPlayerId(
        matchId: Int,
        playerId: Int
    ): BowlerPerformance?

    fun findByMatchIdAndPlayerIdIn(
        matchId: Int,
        playerIds: List<Int>
    ): List<BowlerPerformance?>
}
package com.hit11.zeus.model

import com.sun.org.apache.xpath.internal.operations.Bool
import javax.persistence.*

@Entity
@Table(name = "innings")
data class Inning(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int,
    val inningNumber: Int // 1 for first innings, 2 for second innings, etc.
)

@Entity
@Table(name = "scores")
data class Score(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int,
    val inningId: Int,
    val batmanId : Int = 0,
    val bowlerId : Int = 0,
    val batsmanRuns: Int = 0,
    val extraRuns: Int = 0,
    val overNumber: Int,
    val ballNumber: Int,
    val totalRuns: Int = 0, // Total team runs after this ball
    val totalWickets: Int = 0, // Total team wickets after this ball
    val totalExtras: Int = 0, // Total team extras after this ball
    val isWicket: Boolean = false,
    val wicketType: String? = null,
    val isWide: Boolean = false,
    val isNoBall: Boolean = false,
    val isBye: Boolean = false,
    val isLegBye: Boolean = false,
    val isPenalty: Boolean = false,
    val isSix: Boolean = false,
    val isFour: Boolean = false
)

@Entity
@Table(name = "batsman_performances")
data class BatsmanPerformance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int,
    val inningId: Int,
    val playerId: Int,
    var ballsFaced: Int = 0,
    var runsScored: Int = 0,
    var fours: Int = 0,
    var sixes: Int = 0,
    var howOut: String? = null,
    var bowlerId: Int? = null,
    var fielderId: Int? = null,
    var wicketkeeperCatch: Boolean = false, // true if the catch was taken by the wicketkeeper
)

@Entity
@Table(name = "bowler_performances")
data class BowlerPerformance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int,
    val inningId: Int,
    val playerId: Int,
    var oversBowled: Double = 0.0,
    var runsConceded: Int = 0,
    var wicketsTaken: Int = 0,
    var wides: Int = 0,
    var noBalls: Int = 0
)
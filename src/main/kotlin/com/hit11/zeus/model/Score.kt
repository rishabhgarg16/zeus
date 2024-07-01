package com.hit11.zeus.model

import javax.persistence.*

@Entity
@Table(name = "innings")
data class Inning(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val matchId: Int,
    val teamId: Int,
    val inningNumber: Int // 1 for first innings, 2 for second innings, etc.
)


@Entity
@Table(name = "scores")
data class Score(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val inningId: Int,
    val overNumber: Int,
    var runs: Int = 0,
    var wickets: Int = 0,
    var wides: Int = 0,
    var noBalls: Int = 0,
    var byes: Int = 0,
    var legByes: Int = 0,
    var penaltyRuns: Int = 0
)

@Entity
@Table(name = "batsman_performances")
data class BatsmanPerformance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val inningId: Int,
    val playerId: Int,
    var ballsFaced: Int = 0,
    var runsScored: Int = 0,
    var fours: Int = 0,
    var sixes: Int = 0,
    var howOut: String? = null,
    var bowlerId: Int? = null
)

@Entity
@Table(name = "bowler_performances")
data class BowlerPerformance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val inningId: Int,
    val playerId: Int,
    var oversBowled: Double = 0.0,
    var runsConceded: Int = 0,
    var wicketsTaken: Int = 0,
    var wides: Int = 0,
    var noBalls: Int = 0
)
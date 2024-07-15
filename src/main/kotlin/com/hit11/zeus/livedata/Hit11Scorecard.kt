package com.hit11.zeus.livedata

import java.math.BigDecimal

data class Hit11Scorecard(
    val matchId: Int,
    val matchDescription: String,
    val matchFormat: String,
    val matchType: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val status: String,
    val result: MatchResult,
    val team1: Team,
    val team2: Team,
    val innings: List<Innings>
)

data class MatchResult(
    val resultType: String,
    val winningTeam: String,
    val winningTeamId: Int,
    val winningMargin: Int,
    val winByRuns: Boolean,
    val winByInnings: Boolean
)

data class Team(
    val id: Int,
    val name: String,
    val shortName: String
)

data class Innings(
    val inningsId: Int,
    val battingTeam: Team,
    val bowlingTeam: Team,
    val totalRuns: Int,
    val wickets: Int,
    val totalExtras: Int,
    val overs: BigDecimal,
    val runRate: Float,
    val battingPerformances: List<BattingPerformance>,
    val bowlingPerformances: List<BowlingPerformance>,
    val fallOfWickets: List<FallOfWicket>,
    val partnerships: List<Partnership>,
    val ballByBallEvents: List<BallEvent>
)

data class BattingPerformance(
    val playerId: Int,
    val playerName: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val strikeRate: Float,
    val outDescription: String?,
    val wicketTaker: Int?
)

data class BowlingPerformance(
    val playerId: Int,
    val playerName: String,
    val overs: Float,
    val maidens: Int,
    val runs: Int,
    val wickets: Int,
    val economy: BigDecimal,
    val noBalls: Int,
    val wides: Int
)

data class FallOfWicket(
    val wicketNumber: Int,
    val playerOut: Int,
    val playerName: String,
    val runs: Int,
    val overs: Float
)

data class Partnership(
    val runs: Int,
    val balls: Int,
    val player1Id: Int,
    val player1Name: String,
    val player1Runs: Int,
    val player2Id: Int,
    val player2Name: String,
    val player2Runs: Int
)

data class BallEvent(
    val inningsId: Int,
    val overNumber: Int,
    val ballNumber: Int,
    val batsmanId: Int,
    val bowlerId: Int,
    val runsScored: Int,
    val extraType: String?,
    val extraRuns: Int,
    val isWicket: Boolean,
    val wicketType: String?,
    val playerOutId: Int?,
    val isWide: Boolean = false,
    val isNoBall: Boolean = false,
    val isBye: Boolean = false,
    val isLegBye: Boolean = false,
    val isPenalty: Boolean = false
)

enum class WicketType(val text: String) {
    RUN_OUT("Run Out"),
    CAUGHT("Caught"),
    BOWLED("Bowled"),
    LBW("LBW"),
    STUMPED("Stumped"),
    HIT_WICKET("Hit Wicket"),
    OBSTRUCTED("Obstructed"),
    INVALID("Invalid")
}
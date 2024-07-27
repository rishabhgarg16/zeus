package com.hit11.zeus.livedata

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class Hit11Scorecard(
    val matchId: Int = 0,
    val matchDescription: String = "",
    val matchFormat: String = "",
    val matchType: String = "",
    val startTimestamp: Long = 0,
    val endTimestamp: Long = 0,
    val status: String = "",
    val state: String = "", // Complete, etc ...
    val result: MatchResult = MatchResult(),
    val team1: Team = Team(),
    val team2: Team = Team(),
    val innings: Innings = Innings(),
    val playerOfTheMatch: PlayerOfTheMatch = PlayerOfTheMatch(),
    val tossResult: TossResult? = null
)


data class TossResult(
    val tossWinnerTeamId: Int = 0,
    val tossWinnerName: String = "",
    val tossDecision: String = "",
)

data class PlayerOfTheMatch(
    val id: Int = 0,
    val name: String = "",
    val teamName: String = ""
)

data class MatchResult(
    val resultType: String = "",
    val winningTeam: String = "",
    val winningTeamId: Int = 0,
    val winningMargin: Int = 0,
    val winByRuns: Boolean = false,
    val winByInnings: Boolean = false
)

data class Team(
    val id: Int = 0,
    val name: String = "",
    val shortName: String = "",
    val teamImageUrl: String = "",
    val cricbuzzTeamId: Int = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Innings(
    val inningsId: Int = 0,
    val battingTeam: Team = Team(),
    val bowlingTeam: Team = Team(),
    val totalRuns: Int = 0,
    val wickets: Int = 0,
    val totalExtras: Int = 0,
    val overs: BigDecimal = BigDecimal.ZERO,
    val runRate: Float = 0f,
    val battingPerformances: List<BattingPerformance> = listOf(),
    val bowlingPerformances: List<BowlingPerformance> = listOf(),
    val fallOfWickets: List<FallOfWicket> = listOf(),
    val partnerships: List<Partnership> = listOf(),
    val ballByBallEvents: List<BallEvent> = listOf()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BattingPerformance(
    val playerId: Int = 0,
    val playerName: String = "",
    val runs: Int = 0,
    val balls: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val strikeRate: Float = 0f,
    val outDescription: String? = null,
    val wicketTaker: Int? = null,
    val onStrike: Int? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BowlingPerformance(
    val playerId: Int = 0,
    val playerName: String = "",
    val overs: Float = 0f,
    val maidens: Int = 0,
    val runs: Int = 0,
    val wickets: Int = 0,
    val economy: BigDecimal = BigDecimal.ZERO,
    val noBalls: Int = 0,
    val wides: Int = 0,
    val onStrike: Int? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class FallOfWicket(
    val wicketNumber: Int = 0,
    val playerOut: Int = 0,
    val playerName: String = "",
    val runs: Int = 0,
    val overs: Float = 0f
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Partnership(
    val runs: Int = 0,
    val balls: Int = 0,
    val player1Id: Int = 0,
    val player1Name: String = "",
    val player1Runs: Int = 0,
    val player2Id: Int = 0,
    val player2Name: String = "",
    val player2Runs: Int = 0
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BallEvent(
    val inningsId: Int = 0,
    val overNumber: Int = 0,
    val ballNumber: Int = 0,
    val batsmanId: Int = 0,
    val bowlerId: Int = 0,
    val runsScored: Int = 0,
    val extraType: String? = null,
    val extraRuns: Int = 0,
    val isWicket: Boolean = false,
    val wicketType: String? = null,
    val playerOutId: Int? = null,
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
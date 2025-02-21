package com.hit11.zeus.livedata

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.hit11.zeus.model.CricbuzzMatchPlayingState
import com.hit11.zeus.model.MatchFormat
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
data class Hit11Scorecard(
    val matchId: Int = 0,
    val matchDescription: String = "",
    val matchType: String = "",  // domestic, international, league etc
    val matchFormat: MatchFormat = MatchFormat.T20, // ODI, TEST
    val startTimestamp: Long = 0,
    val endTimestamp: Long = 0,
    val status: String = "", // string status, india need X runs to win
    val state: CricbuzzMatchPlayingState = CricbuzzMatchPlayingState.IN_PROGRESS,
    val result: MatchResult? = null,
    val team1: Team = Team(),
    val team2: Team = Team(),
    val innings: List<Innings> = listOf(),
    val playerOfTheMatch: PlayerOfTheMatch? = null,
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
    @JsonProperty("inningsId") val inningsId: Int = 0,
    @JsonProperty("isCurrentInnings") val isCurrentInnings: Boolean = false,
    @JsonProperty("battingTeam") val battingTeam: Team? = null,
    @JsonProperty("bowlingTeam") val bowlingTeam: Team? = null,
    @JsonProperty("totalRuns") val totalRuns: Int = 0,
    @JsonProperty("wickets") val wickets: Int = 0,
    @JsonProperty("totalExtras") val totalExtras: Int = 0,
    @JsonProperty("overs") val overs: BigDecimal = BigDecimal.ZERO,  // e.g., 31.3
    @JsonProperty("runRate") val runRate: Float = 0f,
    @JsonProperty("battingPerformances") val battingPerformances: List<BattingPerformance> = listOf(),
    @JsonProperty("bowlingPerformances") val bowlingPerformances: List<BowlingPerformance> = listOf(),
    @JsonProperty("fallOfWickets") val fallOfWickets: List<FallOfWicket> = listOf(),
    @JsonProperty("partnerships") val partnerships: List<Partnership> = listOf(),
    @JsonProperty("ballByBallEvents") val ballByBallEvents: List<BallEvent> = listOf()
) {
    // Helper method for total balls calculation
    fun totalBalls(): Int {
        val oversValue = overs.toDouble()
        val completeOvers = oversValue.toInt()
        val balls = ((oversValue % 1) * 10).toInt()
        return (completeOvers * 6) + balls
    }
}

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
    val wicketTaker: String? = null,
    val onStrike: Int? = null,
    val dismissed: Boolean = false
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
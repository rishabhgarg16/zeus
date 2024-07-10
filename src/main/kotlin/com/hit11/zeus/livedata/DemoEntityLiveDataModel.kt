package com.hit11.zeus.livedata

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class LiveMatchResponse(
    val status: String,
    val response: LiveMatchData
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LiveMatchData(
    val mid: Int,
    val status: Int,
    @JsonProperty("status_str") val statusStr: String,
    @JsonProperty("game_state") val gameState: Int,
    @JsonProperty("game_state_str") val gameStateStr: String,
    @JsonProperty("status_note") val statusNote: String,
    @JsonProperty("team_batting") val teamBatting: String,
    @JsonProperty("team_bowling") val teamBowling: String,
    @JsonProperty("live_inning_number") val liveInningNumber: Int,
    @JsonProperty("live_score") val liveScore: LiveScore,
    val batsmen: List<Batsman>,
    val bowlers: List<Bowler>,
    val commentaries: List<Commentary>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LiveScore(
    val runs: Int,
    val overs: Double,
    val wickets: Int,
    val target: Int,
    @JsonProperty("runrate") val runRate: Double,
    @JsonProperty("required_runrate") val requiredRunRate: Double
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Batsman(
    val name: String,
    @JsonProperty("batsman_id") val batsmanId: Int,
    val runs: Int,
    @JsonProperty("balls_faced") val ballsFaced: Int,
    val fours: Int,
    val sixes: Int,
    @JsonProperty("strike_rate") val strikeRate: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Bowler(
    val name: String,
    @JsonProperty("bowler_id") val bowlerId: Int,
    val overs: Double,
    @JsonProperty("runs_conceded") val runsConceded: Int,
    val wickets: Int,
    val maidens: Int,
    val econ: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Commentary(
    @JsonProperty("event_id") val eventId: String?,
    val event: String,
    @JsonProperty("batsman_id") val batsmanId: String,
    @JsonProperty("bowler_id") val bowlerId: String,
    val over: String,
    val ball: String?,
    val score: String,
    val commentary: String,
    val timestamp: Long,
    val run: Int,
    val noball: Boolean,
    val wideball: Boolean,
    val six: Boolean,
    val four: Boolean,
    val wicket_batsman_id: String?,
    val how_out: String?,
    val batsman_runs: String?,
    val batsman_balls: String?
)

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
    val matchId: Int,
    val status: Int,
    val statusStr: String,
    val gameState: Int,
    val gameStateStr: String,
    val statusNote: String,
    val teamBatting: String,
    val teamBowling: String,
    val liveInningNumber: Int,
    val liveScore: LiveScore,
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
    val batsmanId: Int,
    val runs: Int,
    val ballsFaced: Int,
    val fours: Int,
    val sixes: Int,
    val strikeRate: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Bowler(
    val name: String,
    val bowlerId: Int,
    val overs: Double,
    val runsConceded: Int,
    val wickets: Int,
    val maidens: Int,
    val econ: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Commentary(
    val eventId: String?,
    val event: String,
    val batsmanId: String,
    val bowlerId: String,
    val over: String,
    val ball: String?,
    val score: String,
    val commentary: String,
    val timestamp: Long,
    val run: Int,
    val noBall: Boolean,
    val wideBall: Boolean,
    val six: Boolean,
    val four: Boolean,
    val wicketBatsmanId: String?,
    val outDescription: String?,
    val batsmanRuns: String?,
    val batsmanBalls: String?
)

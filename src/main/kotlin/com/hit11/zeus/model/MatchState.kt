package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.hit11.zeus.livedata.Hit11Scorecard

data class MatchState(
//    val currentInning: Inning,
    val liveScorecard: Hit11Scorecard,
//    val currentBallEvent: BallEventEntity
)

enum class CricbuzzMatchPlayingState(val state: String) {
    @JsonProperty("Preview")
    PREVIEW("Preview"),

    @JsonProperty("Complete")
    COMPLETE("Complete"),

    @JsonProperty("In Progress")
    IN_PROGRESS("In progress"),

    @JsonProperty("Toss")
    TOSS("Toss"),

    @JsonProperty("Stumps")
    STUMPS("Stumps")
}

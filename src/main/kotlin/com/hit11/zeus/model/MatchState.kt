package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.hit11.zeus.livedata.Hit11Scorecard

data class MatchState(
//    val currentInning: Inning,
    val liveScorecard: Hit11Scorecard,
//    val currentBallEvent: BallEventEntity
)

enum class CricbuzzMatchPlayingState(val state: String) {
    @JsonProperty("Scheduled")
    SCHEDULED("Scheduled"),

    @JsonProperty("Innings Break")
    INNINGS_BREAK("Innings Break"),

    @JsonProperty("Preview")
    PREVIEW("Preview"),

    @JsonProperty("Drink")
    DRINK("Drink"),

    @JsonProperty("Complete")
    COMPLETE("Complete"),

    @JsonProperty("In Progress")
    IN_PROGRESS("In progress"),

    @JsonProperty("Toss")
    TOSS("Toss"),

    @JsonProperty("Stumps")
    STUMPS("Stumps"),

    @JsonProperty("Tea")
    TEA("Tea"),

    @JsonProperty("Abandon")
    ABANDON("Abandon")
}

fun getCricbuzzMatchPlayingState(state: String): CricbuzzMatchPlayingState {
    for (value in CricbuzzMatchPlayingState.values()) {
        if (value.state == state) {
            return value
        }
    }
    return CricbuzzMatchPlayingState.IN_PROGRESS
}

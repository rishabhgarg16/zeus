package com.hit11.zeus.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BallEvent(
    val inningId: Int = 0,
    val teamName: String = "",
    val matchId: Int = 0,
    val batsmanId: Int = 0,
    val bowlerId: Int = 0,
    val batsmanRuns: Int = 0,
    val extraRuns: Int = 0,
    val overNumber: Int = 0,
    val ballNumber: Int = 0,
    val runsScored: Int = 0,
    val wicketType: String? = null,
    val fielderId: Int? = null,  // ID of the player who caught the ball, if applicable
    val wicketkeeperCatch: Boolean = false, // true if the catch was taken by the wicketkeeper
    val isWicket: Boolean = false,
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
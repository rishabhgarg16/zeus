package com.hit11.zeus.model

import com.hit11.zeus.livedata.Hit11Scorecard

data class MatchState(
    val currentInning: Inning,
    val liveScorecard: Hit11Scorecard,
    val currentBallEvent: BallEventEntity
)
package com.hit11.zeus.livedata

interface DataSource {
    fun fetchMatchData(matchId: Int): String
}

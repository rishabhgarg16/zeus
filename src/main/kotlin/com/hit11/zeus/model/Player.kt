package com.hit11.zeus.model

data class Player(
    val playerId: Int = 0,
    val name: String = "",
    val stat: Int = 0,
    val selectPerc: String = "",
    val credits: Int = 0,
    val iconUrl: String = "",
    val teamName: String = "",
    val role: PlayerRole = PlayerRole.BATTER,
    //    var selected: Boolean = false,
)

enum class PlayerRole {
    BATTER, BOWLER, ALL_ROUNDER, WICKETKEEPER
}
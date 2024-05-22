package com.hit11.zeus.model

data class Match(
    val id: String,
    val title: String,
    val team1: String,
    val team2: String,
    val time: Long,
    val location: String,
    val prize: String,
    val matchType: String
)

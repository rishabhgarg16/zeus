package com.hit11.zeus.model

class UserTeam(
    val id: String = "",
    val userId: String = "", // Foreign key to the User entity
    val name: String = "", // Name given by the user to the fantasy team
    val players: List<Player> = ArrayList(), // List of players selected by the user
    val captain: Player = Player(), // Captain of the fantasy team
    val viceCaptain: Player = Player(), // Vice-captain of the fantasy team
    val matchId: Int = 0,
)
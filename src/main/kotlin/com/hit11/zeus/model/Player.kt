package com.hit11.zeus.model

import javax.persistence.*

@Entity
@Table(name = "players")
data class Player(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val name: String = "",
    val age: Int? = 0,
    val runsScored: Int? = 0,
    val runsConceded: Int? = 0,
    val credits: Int? = 0,
    val iconUrl: String? = "",
    val country: String? = "",
    val teamId: Int = 0,

    @Column(name = "role")
    private var _role: String? = PlayerRole.BATSMAN.text,

    val battingStyle: String? = "",
    val bowlingStyle: String? = "",
) {
    var role: PlayerRole
        get() = PlayerRole.fromText(_role)
        set(value) {
            _role = value.text
        }

}

enum class PlayerRole(val text: String) {
    BATSMAN("batsman"),
    BOWLER("bowler"),
    ALL_ROUNDER("all_rounder"),
    WICKETKEEPER("wicket_keeper"),
    INVALID("invalid");

    companion object {
        fun fromText(text: String?): PlayerRole {
            return entries.find { it.text.equals(text, ignoreCase = true) } ?: INVALID
        }
    }
}
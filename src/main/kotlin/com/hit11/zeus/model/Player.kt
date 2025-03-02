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

    @Enumerated
    @Column(name = "role", columnDefinition = "ENUM('batsman','bowler','all_rounder','wicket_keeper','invalid')")
    private var role: PlayerRole? = PlayerRole.BATSMAN,

    val battingStyle: String? = "",
    val bowlingStyle: String? = "",
)

enum class PlayerRole(val text: String) {
    BATSMAN("batsman"),
    BOWLER("bowler"),
    ALL_ROUNDER("all_rounder"),
    WICKETKEEPER("wicket_keeper"),
    INVALID("invalid");

    companion object {
        fun fromText(text: String?): PlayerRole {
            return values().find { it.text.equals(text, ignoreCase = true) } ?: INVALID
        }
    }
}
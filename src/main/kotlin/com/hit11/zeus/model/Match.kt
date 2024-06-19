package com.hit11.zeus.model

import java.time.Instant
import javax.persistence.*

data class Match(
    val id: Int = 0,
    val matchGroup: String? = null,
    val team1: String = "",
    val team1ImageUrl: String? = null,
    val team2: String = "",
    val team2ImageUrl: String? = null,
    val city: String? = null,
    val stadium: String? = null,
    val country: String? = null,
    val enabled: Boolean = true,
    val tournamentName: String? = null,
    val matchType: String? = null,
    val matchStatus: String? = null,
    val matchLink: String? = null,
    val startDate: Instant = Instant.now(),
    val endDate: Instant = Instant.now(),
    val team1ShortName: String? = null,
    val team2ShortName: String? = null
)


@Entity
@Table(name = "matches")
data class MatchEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    val matchGroup: String? = null,
    val team1: String = "",
    @Column(name = "team_1_image_url")
    val team1ImageUrl: String? = null,
    val team2: String = "",
    @Column(name = "team_2_image_url")
    val team2ImageUrl: String? = null,
    val matchStatus: String? = null,
    val startDate: Instant = Instant.now(),
    @Column(name = "team1_short_name")
    val team1ShortName: String? = null,
    @Column(name = "team2_short_name")
    val team2ShortName: String? = null,
    val city: String? = null,
    val stadium: String? = null,
    val country: String? = null,
    val status: Boolean = false,
    val tournamentName: String? = null,
    val matchType: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now(),


    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()

) {
    @PrePersist
    fun prePersist() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}

fun mapToMatch(matchEntity: MatchEntity): Match {

    return Match(
        id = matchEntity.id,
        matchGroup = matchEntity.matchGroup,
        team1 = matchEntity.team1,
        team1ImageUrl = matchEntity.team1ImageUrl,
        team2 = matchEntity.team2,
        team2ImageUrl = matchEntity.team2ImageUrl,
        city = matchEntity.city,
        stadium = matchEntity.stadium,
        country = matchEntity.country,
        enabled = matchEntity.status,
        tournamentName = matchEntity.tournamentName,
        matchType = matchEntity.matchType,
        matchStatus = matchEntity.matchStatus,
        startDate = matchEntity.startDate,
        team1ShortName = matchEntity.team1ShortName,
        team2ShortName = matchEntity.team2ShortName
    )
}

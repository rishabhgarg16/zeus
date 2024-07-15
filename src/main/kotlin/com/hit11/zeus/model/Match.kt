package com.hit11.zeus.model

import java.time.Instant
import javax.persistence.*

enum class MatchStatus(val text: String) {
    SCHEDULED("Scheduled"),
    ENDED("Ended"),
    DELAYED("Delayed"),
    LIVE("Live")
}

data class Match(
    val id: Int = 0,
    val team1: String = "",
    val team2: String = "",
    val matchGroup: String? = null,
    val city: String? = null,
    val stadium: String? = null,
    val country: String? = null,
    val tournamentName: String? = null,
    val matchType: String? = null,
    val matchStatus: String = "",
    val matchLink: String? = null,
    val startDate: Instant = Instant.now(),
    val endDate: Instant = Instant.now(),
    val team1Id: Int = 0,
    val team2Id: Int = 0,
) {
    fun mapToEntity(): MatchEntity {
        return MatchEntity(
            id = id,
            matchGroup = matchGroup,
            startDate = startDate,
            endDate = endDate,
            city = city,
            stadium = stadium,
            country = country,
            status = matchStatus,
            tournamentName = tournamentName,
            matchType = matchType,
            matchLink = matchLink,
            team1Id = team1Id,
            team2Id = team2Id,
        )
    }
}


@Entity
@Table(name = "matches")
data class MatchEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val team1: String = "",
    val team2: String = "",
    val matchGroup: String? = null,
    val startDate: Instant = Instant.now(),
    val endDate: Instant = Instant.now(),
    val city: String? = null,
    val stadium: String? = null,
    val country: String? = null,
    val status: String = MatchStatus.SCHEDULED.text,
    val tournamentName: String? = null,
    val matchType: String? = null,
    val matchLink: String? = null,
    @Column(name = "team1_id")
    val team1Id: Int = 0,
    @Column(name = "team2_id")
    val team2Id: Int = 0,
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


    fun mapToMatch(): Match {
        return Match(
            id = this.id,
            matchGroup = this.matchGroup,
            team1 = this.team1,
            team2 = this.team2,
            city = this.city,
            stadium = this.stadium,
            country = this.country,
            tournamentName = this.tournamentName,
            matchType = this.matchType,
            matchStatus = findMatchStatus(this.startDate),
            startDate = this.startDate,
            endDate = this.endDate,
            team1Id = this.team1Id,
            team2Id = this.team2Id,
            matchLink = this.matchLink,
        )
    }

    private fun findMatchStatus(startDate: Instant): String {
        if (startDate < Instant.now()) {
            return MatchStatus.LIVE.text
        } else {
            return MatchStatus.SCHEDULED.text
        }
    }
}

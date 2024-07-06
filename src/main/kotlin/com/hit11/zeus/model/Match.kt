package com.hit11.zeus.model

import java.time.Instant
import javax.persistence.*

enum class MatchStatus (val text: String) {
    SCHEDULED("Scheduled"),
    ENDED("Ended"),
    DELAYED("Delayed"),
    LIVE("Live")
}

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
    val tournamentName: String? = null,
    val matchType: String? = null,
    val matchStatus: String = "",
    val matchLink: String? = null,
    val startDate: Instant = Instant.now(),
    val endDate: Instant = Instant.now(),
    val team1ShortName: String? = null,
    val team2ShortName: String? = null,
    var currentInningId: Int? = null, // The current active innings
    var team1Score: Int = 0,
    var team2Score: Int = 0,
    var team1Wickets: Int = 0,
    var team2Wickets: Int = 0

) {
    fun mapToEntity(): MatchEntity {
        return MatchEntity(
            id = id,
            matchGroup = matchGroup,
            team1 = team1,
            team1ImageUrl = team1ImageUrl,
            team2 = team2,
            team2ImageUrl = team2ImageUrl,
            startDate = startDate,
            endDate = endDate,
            team1ShortName = team1ShortName,
            team2ShortName = team2ShortName,
            city = city,
            stadium = stadium,
            country = country,
            status = matchStatus,
            tournamentName = tournamentName,
            matchType = matchType,
            matchLink = matchLink,
            currentInningId = currentInningId?: 0,
            team1Score = team1Score,
            team2Score = team2Score,
            team1Wickets = team1Wickets,
            team2Wickets = team2Wickets
        )
    }
}




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
    val startDate: Instant = Instant.now(),
    val endDate: Instant = Instant.now(),
    @Column(name = "team1_short_name")
    val team1ShortName: String? = null,
    @Column(name = "team2_short_name")
    val team2ShortName: String? = null,
    val city: String? = null,
    val stadium: String? = null,
    val country: String? = null,
    val status: String = MatchStatus.SCHEDULED.text,
    val tournamentName: String? = null,
    val matchType: String? = null,
    val matchLink: String? = null,
    @Column(name = "current_inning_id")
    var currentInningId: Int? = null,
    @Column(name = "team1_score")
    val team1Score: Int = 0,
    @Column(name = "team2_score")
    val team2Score: Int = 0,
    @Column(name = "team1_wickets")
    val team1Wickets: Int = 0,
    @Column(name = "team2_wickets")
    val team2Wickets: Int = 0,

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
            team1ImageUrl = this.team1ImageUrl,
            team2 = this.team2,
            team2ImageUrl = this.team2ImageUrl,
            city = this.city,
            stadium = this.stadium,
            country = this.country,
            tournamentName = this.tournamentName,
            matchType = this.matchType,
            matchStatus = findMatchStatus(this.startDate),
            startDate = this.startDate,
            endDate = this.endDate,
            team1ShortName = this.team1ShortName,
            team2ShortName = this.team2ShortName,
            currentInningId = this.currentInningId,
            team1Score = this.team1Score,
            team2Score = this.team2Score,
            team1Wickets = this.team1Wickets,
            team2Wickets = this.team2Wickets
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

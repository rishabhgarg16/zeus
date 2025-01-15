package com.hit11.zeus.model

import java.time.Instant
import javax.persistence.*

enum class MatchStatus(val text: String) {
    SCHEDULED("Scheduled"),
    PREVIEW("Preview"),
    COMPLETE("Complete"),
    INNINGS_BREAK("Innings Break"),
    TEA("Tea"),
    TOSS("Toss"),
    STUMPS("Stumps"),
    IN_PROGRESS("In Progress");

    companion object {
        fun fromText(text: String): MatchStatus = values().find { it.text == text }
            ?: throw IllegalArgumentException("No MatchStatus found for text: $text")
    }
}

enum class MatchFormat(matchFormat: String) {
    ODI("ODI"),
    T20("T20"),
    TEST("TEST")
}

@Entity
@Table(name = "matches")
data class Match(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false)
    val team1: String = "",

    @Column(nullable = false)
    val team2: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_id", insertable = false, updatable = false)
    val team1Entity: TeamEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_id", insertable = false, updatable = false)
    val team2Entity: TeamEntity? = null,

    val matchGroup: String? = null,
    val startDate: Instant = Instant.now(),
    val endDate: Instant = Instant.now(),
    val city: String? = null,
    val stadium: String? = null,
    val country: String? = null,
    val status: String = MatchStatus.SCHEDULED.text,
    val tournamentName: String? = null,
    val matchFormat: String? = null,
    val matchType: String? = null,
    val matchLink: String? = null,

    @Column(name = "team1_id")
    val team1Id: Long = 0,

    @Column(name = "team2_id")
    val team2Id: Long = 0,

    @Column(name = "cricbuzz_match_id")
    val cricbuzzMatchId: Int? = 0,

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

    // Helper properties for frequently used combinations
    val team1ShortName: String get() = team1Entity?.teamShortName ?: ""
    val team2ShortName: String get() = team2Entity?.teamShortName ?: ""
    val team1Name: String get() = team1Entity?.teamName ?: ""
    val team2Name: String get() = team2Entity?.teamName ?: ""
    val team1ImageUrl: String? get() = team1Entity?.teamImageUrl
    val team2ImageUrl: String? get() = team2Entity?.teamImageUrl

    // Helper for match title
    val matchTitle: String get() = "$team1ShortName vs $team2ShortName"
}
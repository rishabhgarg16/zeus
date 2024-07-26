package com.hit11.zeus.model

import java.time.Instant
import javax.persistence.*

@Entity
@Table(name = "teams")
data class TeamEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(name = "team_name", nullable = false)
    val teamName: String = "",
    
    @Column(name = "team_short_name", nullable = false)
    val teamShortName: String = "",
    
    @Column(name = "team_image_url")
    val teamImageUrl: String? = null,

    @Column(name = "cricbuzz_team_id")
    val cricbuzzTeamId: Int? = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = Instant.now()
    }
}
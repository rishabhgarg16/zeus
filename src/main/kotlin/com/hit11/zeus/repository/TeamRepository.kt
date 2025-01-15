package com.hit11.zeus.repository

import com.hit11.zeus.model.TeamEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamRepository : JpaRepository<TeamEntity, Long> {
    fun findByTeamName(teamName: String): TeamEntity?
    fun findByTeamShortName(teamShortName: String): TeamEntity?
    fun findByCricbuzzTeamId(cricbuzzTeamId: Int): TeamEntity?
}
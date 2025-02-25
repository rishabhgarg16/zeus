package com.hit11.zeus.repository

import com.hit11.zeus.model.TeamEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TeamRepository : JpaRepository<TeamEntity, Long> {
    fun findByTeamName(teamName: String): TeamEntity?

    fun findByTeamShortName(teamShortName: String): TeamEntity?

    fun findByCricbuzzTeamId(cricbuzzTeamId: Int): TeamEntity?

    @Query("SELECT t FROM TeamEntity t WHERE t.cricbuzzTeamId IN :cricbuzzTeamIds")
    fun findAllByCricbuzzTeamIdIn(@Param("cricbuzzTeamIds") cricbuzzTeamIds: List<Int>): List<TeamEntity>

    @Query("SELECT t FROM TeamEntity t WHERE t.id = :id")
    fun findByIdWithDetails(@Param("id") id: Long): TeamEntity?
}
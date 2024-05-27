package com.hit11.zeus.service

import com.hit11.zeus.model.Team
import com.hit11.zeus.repository.TeamRepository
import org.springframework.stereotype.Service

@Service
class TeamService(private val teamRepository: TeamRepository) {

    fun getUserTeams(userId: Int, matchId: Int): List<Team> {
        TODO("Not yet implemented")
    }
}

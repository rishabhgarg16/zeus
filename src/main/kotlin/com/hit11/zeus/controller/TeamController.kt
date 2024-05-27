package com.hit11.zeus.controller


import com.hit11.zeus.model.Team
import com.hit11.zeus.service.PlayerService
import com.hit11.zeus.service.TeamService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/team")
class TeamController(
    private val playerService: PlayerService,
    private val teamService: TeamService
) {
    @GetMapping("/user/{userId}/match/{matchId}")
    fun getUserTeamsForMatch(
        @PathVariable userId: Int,
        @PathVariable matchId: Int
    ): List<Team> {
        return teamService.getUserTeams(userId, matchId)
    }


    @PostMapping("/create")
    suspend fun saveTeam(@RequestBody team: Team) {
        TODO()
    }
}
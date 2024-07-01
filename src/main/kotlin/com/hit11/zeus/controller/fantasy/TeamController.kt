package com.hit11.zeus.controller.fantasy


import com.hit11.zeus.model.UserTeam
import com.hit11.zeus.service.PlayerService
import com.hit11.zeus.service.TeamService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/team")
class TeamController(
    private val playerService: PlayerService,
    private val teamService: TeamService
) {
    @GetMapping("/user/{userId}/match/{matchId}")
    fun getUserTeamsForMatch(
        @PathVariable userId: String,
        @PathVariable matchId: Int
    ): List<UserTeam> {
        return teamService.getUserTeams(userId, matchId)
    }


    @PostMapping("/create")
    fun saveTeam(@RequestBody userTeam: UserTeam): Any? {
        if (userTeam.matchId == 0 || userTeam.userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("bad request");
        }
        val team = teamService.saveUserTeam(userTeam)
                ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal server error")
        return team
    }
}
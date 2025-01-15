package com.hit11.zeus.controller.fantasy


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

}
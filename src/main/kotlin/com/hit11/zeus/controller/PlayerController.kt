package com.hit11.zeus.controller


import com.hit11.zeus.model.Player
import com.hit11.zeus.service.PlayerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/player")
class PlayerController(private val playerService: PlayerService) {

    @GetMapping("/match/{matchId}")
    fun getPlayersForMatch(
        @PathVariable matchId: Int
    ): List<Player> {
        return playerService.getPlayerList() //TODO
    }
}
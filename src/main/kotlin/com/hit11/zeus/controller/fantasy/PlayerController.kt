package com.hit11.zeus.controller.fantasy


import com.hit11.zeus.model.Player
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.service.PlayerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/player")
class PlayerController(private val playerService: PlayerService) {

    @GetMapping("/{country}")
    fun getAllPlayerByCountry(
        @PathVariable country: String,
    ): ApiResponse<List<Player>> {
        val playerList = playerService.getPlayerListByCountry(country)
        return ApiResponse(
            data = playerList,
            message = "Success",
            status = 200,
            internalCode = null
        )
    }
}
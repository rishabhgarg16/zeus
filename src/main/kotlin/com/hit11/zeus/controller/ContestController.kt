package com.hit11.zeus.controller


import com.hit11.zeus.model.Contest
import com.hit11.zeus.service.ContestService
import com.hit11.zeus.service.PlayerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contest")
class ContestController(
    private val contestService: ContestService
) {

    @GetMapping("/match/{matchId}")
    fun getContestForMatch(
        @PathVariable matchId: Int
    ): List<Contest> {
        return contestService.getContestForMatch(matchId)
    }
}
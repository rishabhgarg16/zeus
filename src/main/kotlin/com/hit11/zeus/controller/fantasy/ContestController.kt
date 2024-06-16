package com.hit11.zeus.controller.fantasy


import com.hit11.zeus.model.Contest
import com.hit11.zeus.service.ContestService
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

    @GetMapping("/match/{matchId}/user/{userId}")
    fun getUserEnrolledContests(
        @PathVariable("matchId") matchId: Int,
        @PathVariable("userId") userId: String
    ): List<Contest> {
        return contestService.getContestForMatch(matchId) // TODO change this
    }
}
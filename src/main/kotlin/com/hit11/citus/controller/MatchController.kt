package com.hit11.citus.controller

import com.hit11.citus.model.Match
import com.hit11.citus.service.MatchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/matches")
class MatchController(private val matchService: MatchService) {

    @GetMapping("/upcoming")
    fun getUpcomingMatches(): List<Match> {
        return matchService.getUpcomingMatches()
    }
}

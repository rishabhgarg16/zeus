package com.hit11.zeus.controller

import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.Match
import com.hit11.zeus.model.external.CricbuzzMatchResponse
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.model.response.GetMatchApiResponse
import com.hit11.zeus.model.response.toGetMatchApiResponse
import com.hit11.zeus.service.CricbuzzMatchService
import com.hit11.zeus.service.MatchService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/match")
class MatchController(
    private val matchService: MatchService,
    private val cricbuzzMatchService: CricbuzzMatchService,
) {
    @GetMapping("/upcoming")
    fun getLiveMatches(@RequestParam("limit", defaultValue = "4") limit: Int):
            ResponseEntity<ApiResponse<List<GetMatchApiResponse>>> {
        val data = matchService.getRelevantMatches(limit).map { it.toGetMatchApiResponse() }
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = data
            )
        )
    }

    @GetMapping("/criccbuzz/upcoming")
    fun getMatchesFromCricbuzzAPI(): ResponseEntity<ApiResponse<CricbuzzMatchResponse>> {
        return try {
            val cricbuzzMatches = cricbuzzMatchService.getLiveAndUpcomingMatches()
            ResponseEntity.ok(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    internalCode = null,
                    message = "Successfully fetched matches from Cricbuzz",
                    data = cricbuzzMatches
                )
            )
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    internalCode = null,
                    message = "Error fetching matches from Cricbuzz: ${e.message}",
                    data = null
                )
            )
        }
    }

    @GetMapping("/{matchId}")
    fun getMatchById(@PathVariable matchId: Int): ResponseEntity<ApiResponse<Match>> {
        val data = matchService.getMatchById(matchId)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = data
            )
        )
    }

    @GetMapping("/livescore/{matchId}")
    fun getLiveScore(
        @PathVariable matchId: Int,
        @RequestParam(name = "cache", required = false, defaultValue = "true") useCache: Boolean
    ): ResponseEntity<ApiResponse<Hit11Scorecard>> {
        return try {
            val data = matchService.getScoreByMatch(matchId, useCache)

            if (data != null) {
                return ResponseEntity.ok(
                    ApiResponse(
                        status = HttpStatus.OK.value(),
                        internalCode = null,
                        message = "Success",
                        data = data
                    )
                )
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse(
                        status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        internalCode = null,
                        message = "Error fetching live score for match ${matchId}",
                        data = null
                    )
                )
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    internalCode = null,
                    message = "Error fetching live score: ${e.message}",
                    data = null
                )
            )
        }
    }

    @GetMapping("/livescore/cricbuzz/{cricbuzzMatchId}")
    fun getOrCreateLiveScoreByCricbuzz(
        @PathVariable cricbuzzMatchId: Int,
        @RequestParam(name = "cache", required = false, defaultValue = "true") useCache: Boolean
    ): ResponseEntity<ApiResponse<Hit11Scorecard>> {
        val data = matchService.getScoreByCricbuzzMatch(cricbuzzMatchId, useCache)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = data
            )
        )
    }
}
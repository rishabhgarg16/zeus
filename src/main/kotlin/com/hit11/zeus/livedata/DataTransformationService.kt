package com.hit11.zeus.livedata

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.hit11.zeus.model.BallEvent

import org.springframework.stereotype.Service

@Service
class DataTransformationService {

//    fun parseMatchResponse(jsonData: String): MatchResponse {
//        val mapper = jacksonObjectMapper()
//        val apiResponse: ApiResponse = mapper.readValue(jsonData)
//        return apiResponse.response
//    }

//    fun transformToBallEvents(matchResponse: MatchResponse): List<BallEvent> {
//        val ballEvents = mutableListOf<BallEvent>()
//
//        matchResponse.innings.forEach { inning ->
//            inning.balls.forEach { ball ->
//                ballEvents.add(
//                    BallEvent(
//                        inningId = inning.number,
//                        teamName = if (inning.batting_team_id == matchResponse.teama.team_id) matchResponse.teama.name else matchResponse.teamb.name,
//                        matchId = matchResponse.match_id,
//                        batsmanId = ball.batsman_id,
//                        bowlerId = ball.bowler_id,
//                        batsmanRuns = ball.runs_batsman,
//                        extraRuns = ball.runs_extras,
//                        overNumber = ball.over_number,
//                        ballNumber = ball.ball_number,
//                        runsScored = ball.runs_total,
//                        wicketType = ball.wicket_type,
//                        fielderId = ball.fielder_id,
//                        wicketkeeperCatch = ball.wicket_type == "Caught" && ball.fielder_id != null,
//                        isWicket = ball.is_wicket,
//                        isWide = ball.is_wide,
//                        isNoBall = ball.is_noball,
//                        isBye = ball.is_bye,
//                        isLegBye = ball.is_legbye,
//                        isPenalty = ball.is_penalty
//                    )
//                )
//            }
//        }
//
//        return ballEvents
//    }
}

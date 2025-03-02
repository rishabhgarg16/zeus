package com.hit11.zeus.service

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.*
import com.hit11.zeus.model.MatchFormat
import com.hit11.zeus.model.TeamEntity
import com.hit11.zeus.model.external.CricbuzzMatchResponse
import com.hit11.zeus.model.external.TypeMatch
import com.hit11.zeus.model.getCricbuzzMatchPlayingState
import com.hit11.zeus.repository.TeamRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap

@Service
class CricbuzzApiService(
    private val teamRepository: TeamRepository
) {
    val client = OkHttpClient()
    private val logger = Logger.getLogger(CricbuzzApiService::class.java)
    private val apiKey = "812ccf7ceemsh4ccdd29e8208a08p147c6cjsn2235603a694b"
    private var lastLiveFetch: Instant = Instant.MIN
    private var lastUpcomingFetch: Instant = Instant.MIN
    private var lastRecentFetch: Instant = Instant.MIN
    private val apiResponseCache = ConcurrentHashMap<String, Pair<Instant, CricbuzzMatchResponse>>()
    private val apiResponseCacheTTL = 2L // in minutes

    // Configure intervals (adjust based on your API budget)
    private val LIVE_FETCH_INTERVAL_MIN = 2L     // Most critical
    private val UPCOMING_FETCH_INTERVAL_MIN = 10L
    private val RECENT_FETCH_INTERVAL_MIN = 30L  // Least critical

    fun getLiveAndUpcomingMatches(): CricbuzzMatchResponse {
        val combinedMatches = mutableListOf<TypeMatch>()

        try {
            // Fetch live matches
            if (lastLiveFetch.isBefore(Instant.now().minus(LIVE_FETCH_INTERVAL_MIN, ChronoUnit.MINUTES))) {
                try {
                    val liveMatches = fetchMatchesByEndpoint("https://cricbuzz-cricket.p.rapidapi.com/matches/v1/live")
                    combinedMatches.addAll(liveMatches.typeMatches ?: emptyList())
                    lastLiveFetch = Instant.now()
                } catch (e: Exception) {
                    logger.error("Error fetching live matches, continuing with other endpoints", e)
                    // Continue execution - we don't want to fail the entire function if one endpoint fails
                }
            }

            // Fetch upcoming matches
            if (lastUpcomingFetch.isBefore(Instant.now().minus(UPCOMING_FETCH_INTERVAL_MIN, ChronoUnit.MINUTES))) {
                try {
                    val upcomingMatches =
                        fetchMatchesByEndpoint("https://cricbuzz-cricket.p.rapidapi.com/matches/v1/upcoming")
                    combinedMatches.addAll(upcomingMatches.typeMatches ?: emptyList())
                    lastUpcomingFetch = Instant.now()
                } catch (e: Exception) {
                    logger.error("Error fetching upcoming matches, continuing with other endpoints", e)
                }
            }

            if (lastRecentFetch.isBefore(Instant.now().minus(RECENT_FETCH_INTERVAL_MIN, ChronoUnit.MINUTES))) {
                try {
                    val recentMatches =
                        fetchMatchesByEndpoint("https://cricbuzz-cricket.p.rapidapi.com/matches/v1/recent")
                    combinedMatches.addAll(recentMatches.typeMatches ?: emptyList())
                    lastRecentFetch = Instant.now()
                } catch (e: Exception) {
                    logger.error("Error fetching recent matches, continuing with other endpoints", e)
                }
            }

            return CricbuzzMatchResponse(
                typeMatches = combinedMatches,
//                filters = Filters(emptyList()), // Add appropriate filters if needed
//                appIndex = AppIndex("", ""),
                responseLastUpdated = Instant.now().toString()
            )
        } catch (e: Exception) {
            logger.error("Error aggregating matches from Cricbuzz", e)
            return CricbuzzMatchResponse(
                typeMatches = emptyList(),
                responseLastUpdated = Instant.now().toString()
            )
        }
    }

    private fun fetchMatchesByEndpoint(endpoint: String): CricbuzzMatchResponse {
        val cached = apiResponseCache[endpoint]
        if (cached != null && cached.first.isAfter(Instant.now().minus(apiResponseCacheTTL, ChronoUnit.MINUTES))) {
            return cached.second
        }

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("x-rapidapi-host", "cricbuzz-cricket.p.rapidapi.com")
            .addHeader("x-rapidapi-key", apiKey)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                throw IOException("Unexpected response $response")
            }

            if (responseBody == null) {
                throw IOException("Response body is null")
            }

            val gson = Gson()
            try {
                val convertedMatchResponse = gson.fromJson(responseBody, CricbuzzMatchResponse::class.java)

                // Validate essential fields
                if (convertedMatchResponse.typeMatches == null) {
                    logger.warn("API returned null for typeMatches from $endpoint")
                    convertedMatchResponse.copy(typeMatches = emptyList())
                } else {
                    // Store in cache
                    apiResponseCache[endpoint] = Instant.now() to convertedMatchResponse
                    convertedMatchResponse
                }
            } catch (e: JsonSyntaxException) {
                logger.error("Error parsing JSON response from $endpoint", e)
                logger.debug("Response body: $responseBody")
                throw e
            }
        } catch (e: Exception) {
            logger.error("Error fetching matches from endpoint $endpoint", e)

            // Return cached data if available, otherwise rethrow
            cached?.second?.let { return it } ?: throw e
        }
    }

    fun getMatchScore(
        matchId: Int,
        cricbuzzMatchId: Int
    ): Hit11Scorecard? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://cricbuzz-cricket.p.rapidapi.com/mcenter/v1/$cricbuzzMatchId/comm")
            .addHeader("x-rapidapi-host", "cricbuzz-cricket.p.rapidapi.com")
            .addHeader("x-rapidapi-key", "812ccf7ceemsh4ccdd29e8208a08p147c6cjsn2235603a694b")
            .build()
        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                logger.error("Unsuccessful response getting match score: ${response.code}")
                return null
            }

            if (responseBody == null) {
                logger.error("Null response body when getting match score")
                return null
            }

            val gson = Gson()

            try {
                val criccbuzzMatchDataModel =
                    gson.fromJson(responseBody, CriccbuzzLiveScoreDataModel::class.java)
                val team1 = teamRepository.findByCricbuzzTeamId(criccbuzzMatchDataModel.matchHeader.team1.id)
                val team2 = teamRepository.findByCricbuzzTeamId(criccbuzzMatchDataModel.matchHeader.team2.id)
                var hit11ScoreCard = transformMatchDataToHit11Scorecard(matchId, criccbuzzMatchDataModel, team1, team2)
                hit11ScoreCard
            } catch (e: JsonSyntaxException) {
                logger.error("Error parsing JSON response for match score", e)
                logger.debug("Response body: $responseBody")
                null
            } catch (e: NullPointerException) {
                logger.error("Null pointer exception processing match score", e)
                null
            } catch (e: Exception) {
                logger.error("Unexpected error processing match score: ${e.message}", e)
                null
            }
        } catch (e: Exception) {
            logger.error("Error fetching match score: ${e.message}", e)
            null
        }
    }
}

fun transformMatchDataToHit11Scorecard(
    matchId: Int,
    criccbuzzMatchDataModel: CriccbuzzLiveScoreDataModel,
    dbteam1: TeamEntity?,
    dbteam2: TeamEntity?
): Hit11Scorecard {
    val matchHeader = criccbuzzMatchDataModel.matchHeader
    val team1 = matchHeader.team1
    val team2 = matchHeader.team2
    val winningTeam = when {
        matchHeader.result.winningTeam.contentEquals(team1.name) || matchHeader.result.winningTeam.contentEquals
            (team1.shortName) -> team1

        matchHeader.result.winningTeam.contentEquals(team2.name) || matchHeader.result.winningTeam.contentEquals
            (team2.shortName) -> team2

        else -> null
    }

    val matchResult = winningTeam?.let { getMatchResult(matchHeader, winningTeam) }


    val inningsList = criccbuzzMatchDataModel.miniscore?.matchScoreDetails?.inningsScoreList?.map {
        convertInningsScoreToInnings(it, criccbuzzMatchDataModel)
    } ?: emptyList()

    val playerOfTheMatch = matchHeader.playersOfTheMatch.firstOrNull()

    val tossResult = matchHeader.tossResults.takeIf { it.tossWinnerId != 0 }?.let {
        TossResult(
            tossWinnerTeamId = it.tossWinnerId,
            tossWinnerName = it.tossWinnerName,
            tossDecision = it.decision
        )
    }


    val scorecard = Hit11Scorecard(
        matchId = matchId,
        matchDescription = matchHeader.matchDescription,
        matchType = matchHeader.matchType,
        matchFormat = MatchFormat.valueOf(matchHeader.matchFormat),
        startTimestamp = matchHeader.matchStartTimestamp,
        endTimestamp = matchHeader.matchCompleteTimestamp,
        status = matchHeader.status,
        state = getCricbuzzMatchPlayingState(matchHeader.state),
        result = matchResult,
        team1 = Team(
            id = team1.id, name = team1.name, shortName = team1.shortName, teamImageUrl = dbteam1?.teamImageUrl ?: ""
        ),
        team2 = Team(
            id = team2.id, name = team2.name, shortName = team2.shortName, teamImageUrl = dbteam2?.teamImageUrl ?: ""
        ),
        innings = inningsList,
        playerOfTheMatch = playerOfTheMatch,
        tossResult = tossResult
    )
    return scorecard
}

fun getMatchResult(
    matchHeader: MatchHeader,
    winningTeam: CBTeam,
): MatchResult? {
    return matchHeader.result.takeIf { it.winningTeam.isNotEmpty() }?.let {
        MatchResult(
            resultType = it.resultType,
            winningTeam = it.winningTeam,
            winningTeamId = winningTeam.id,
            winByRuns = it.winByRuns,
            winByInnings = it.winByInnings,
            winningMargin = it.winningMargin
        )
    }
}


data class CriccbuzzLiveScoreDataModel(
    val commentaryList: List<Commentary>,
    val matchHeader: MatchHeader,
    val miniscore: Miniscore? = null,
    val commentarySnippetList: List<Any>,
    val page: String,
    val enableNoContent: Boolean,
    val matchVideos: List<Any>,
    val responseLastUpdated: Long,
    val cb11: CB11
)

data class Commentary(
    val commText: String,
    val timestamp: Long,
    val ballNbr: Int,
    val overNumber: Double?,
    val inningsId: Int,
    val event: String,
    val batTeamName: String,
    val commentaryFormats: CommentaryFormats,
    val overSeparator: OverSeparator?
)

data class CommentaryFormats(
    val bold: Bold?
)

data class Bold(
    val formatId: List<String>,
    val formatValue: List<String>
)

data class OverSeparator(
    val score: Int,
    val wickets: Int,
    val inningsId: Int,
    val o_summary: String,
    val runs: Int,
    val batStrikerIds: List<Int>,
    val batStrikerNames: List<String>,
    val batStrikerRuns: Int,
    val batStrikerBalls: Int,
    val batNonStrikerIds: List<Int>,
    val batNonStrikerNames: List<String>,
    val batNonStrikerRuns: Int,
    val batNonStrikerBalls: Int,
    val bowlIds: List<Int>,
    val bowlNames: List<String>,
    val bowlOvers: Double,
    val bowlMaidens: Int,
    val bowlRuns: Int,
    val bowlWickets: Int,
    val timestamp: Long,
    val overNum: Double,
    val event: String
)

data class MatchHeader(
    val matchId: Int,
    val matchDescription: String,
    val matchFormat: String,
    val matchType: String,
    val complete: Boolean,
    val domestic: Boolean,
    val matchStartTimestamp: Long,
    val matchCompleteTimestamp: Long,
    val dayNight: Boolean,
    val year: Int,
    val state: String,
    val status: String,
    val tossResults: TossResults,
    val result: Result,
    val revisedTarget: RevisedTarget,
    val playersOfTheMatch: List<PlayerOfTheMatch>,
    val playersOfTheSeries: List<PlayerOfTheMatch>,
    val matchTeamInfo: List<MatchTeamInfo>,
    val team1: CBTeam,
    val team2: CBTeam,
    val seriesDesc: String,
    val seriesId: Int,
    val seriesName: String,
    val alertType: String,
    val livestreamEnabled: Boolean
)

data class TossResults(
    val tossWinnerId: Int,
    val tossWinnerName: String,
    val decision: String
)

data class Result(
    val resultType: String = "",
    val winningTeam: String = "",
    val winByRuns: Boolean = false,
    val winByInnings: Boolean = false,
    val winningMargin: Int = 0
)

data class RevisedTarget(
    val reason: String
)

data class MatchTeamInfo(
    val battingTeamId: Int,
    val battingTeamShortName: String,
    val bowlingTeamId: Int,
    val bowlingTeamShortName: String
)

data class CBTeam(
    val id: Int,
    val name: String,
    val playerDetails: List<Any>,
    val shortName: String
)

data class Miniscore(
    val inningsId: Int,
    val batsmanStriker: Batsman,
    val batsmanNonStriker: Batsman,
    val batTeam: BatTeam,
    val bowlerStriker: Bowler,
    val bowlerNonStriker: Bowler,
    val overs: Double,
    val recentOvsStats: String,
    val target: Int,
    val partnerShip: Partnership,
    val currentRunRate: Double,
    val requiredRunRate: Double,
    val lastWicket: String,
    val matchScoreDetails: MatchScoreDetails,
    val latestPerformance: List<Performance>,
    val ppData: PowerplayData,
    val matchUdrs: MatchUdrs,
    val overSummaryList: List<Any>,
    val status: String,
    val lastWicketScore: Int,
    val remRunsToWin: Int,
    val event: String
)

data class Batsman(
    val batBalls: Int,
    val batDots: Int,
    val batFours: Int,
    val batId: Int,
    val batName: String,
    val batMins: Int,
    val batRuns: Int,
    val batSixes: Int,
    val batStrikeRate: Double
)

data class BatTeam(
    val teamId: Int,
    val teamScore: Int,
    val teamWkts: Int
)

data class Bowler(
    val bowlId: Int,
    val bowlName: String,
    val bowlMaidens: Int,
    val bowlNoballs: Int,
    val bowlOvs: Double,
    val bowlRuns: Int,
    val bowlWides: Int,
    val bowlWkts: Int,
    val bowlEcon: Double
)

data class Partnership(
    val balls: Int,
    val runs: Int
)

data class MatchScoreDetails(
    val matchId: Int,
    val inningsScoreList: List<InningsScore>,
    val tossResults: TossResults,
    val matchTeamInfo: List<MatchTeamInfo>,
    val isMatchNotCovered: Boolean,
    val matchFormat: String,
    val state: String,
    val customStatus: String,
    val highlightedTeamId: Int
)

data class InningsScore(
    val inningsId: Int,
    val batTeamId: Int,
    val batTeamName: String,
    val score: Int,
    val wickets: Int,
    val overs: Double = 0.0,
    val isDeclared: Boolean,
    val isFollowOn: Boolean,
    val ballNbr: Int
)

fun convertInningsScoreToInnings(
    inningsScore: InningsScore,
    criccbuzzMatchDataModel: CriccbuzzLiveScoreDataModel
): Innings {
    val matchHeader = criccbuzzMatchDataModel.matchHeader
    val cbTeam1 = matchHeader.team1
    val cbTeam2 = matchHeader.team2

    val battingCBTeam = Team(
        id = inningsScore.batTeamId,
        name = when (inningsScore.batTeamId) {
            cbTeam1.id -> cbTeam1.name
            cbTeam2.id -> cbTeam2.name
            else -> throw IllegalArgumentException("Invalid batting team ID")
        },
        shortName = when (inningsScore.batTeamId) {
            cbTeam1.id -> cbTeam1.shortName
            cbTeam2.id -> cbTeam2.shortName
            else -> throw IllegalArgumentException("Invalid batting team ID")
        }
    )

    val bowlingCBTeam = when (inningsScore.batTeamId) {
        cbTeam1.id -> Team(
            id = cbTeam2.id,
            name = cbTeam2.name,
            shortName = cbTeam2.shortName
        )

        else -> Team(
            id = cbTeam1.id,
            name = cbTeam1.name,
            shortName = cbTeam1.shortName
        )
    }

    // Convert overs to BigDecimal with proper formatting
    val oversAsBigDecimal = convertOversToBigDecimal(overs = inningsScore.overs)

    // Calculating the run rate assuming overs are not zero to avoid division by zero error.
    val runRate = if (inningsScore.overs > 0) inningsScore.score / inningsScore.overs.toFloat() else 0f

    return Innings(
        inningsId = inningsScore.inningsId,
        battingTeam = battingCBTeam,
        totalRuns = inningsScore.score,
        wickets = inningsScore.wickets,
        overs = oversAsBigDecimal,
        runRate = runRate,
        bowlingTeam = bowlingCBTeam,
        isCurrentInnings = criccbuzzMatchDataModel.miniscore?.inningsId == inningsScore.inningsId
        // The rest of the fields are initialized with default values or remain null
    )
}

fun convertOversToBigDecimal(overs: Double): BigDecimal {
    val completeOvers = overs.toInt() // Gets the whole overs
    val balls = ((overs % 1) * 10).toInt() // Gets the balls

    // Handle case where balls reach 6
    val adjustedOvers = if (balls >= 6) {
        completeOvers + 1
    } else {
        completeOvers
    }

    val adjustedBalls = if (balls >= 6) {
        0
    } else {
        balls
    }

    return BigDecimal("$adjustedOvers.$adjustedBalls")
}


data class Performance(
    val runs: Int,
    val wkts: Int,
    val label: String
)

data class PowerplayData(
    val pp_1: Powerplay
)

data class Powerplay(
    val ppId: Int,
    val ppOversFrom: Double,
    val ppOversTo: Double,
    val ppType: String,
    val runsScored: Int
)

data class MatchUdrs(
    val matchId: Int,
    val inningsId: Int,
    val timestamp: String,
    val team1Id: Int,
    val team1Remaining: Int,
    val team1Successful: Int,
    val team1Unsuccessful: Int,
    val team2Id: Int,
    val team2Remaining: Int,
    val team2Successful: Int,
    val team2Unsuccessful: Int
)

data class CB11(
    val team1Sname: String,
    val team2Sname: String,
    val title: String,
    val imageId: Int,
    val appLink: String,
    val webLink: String
)

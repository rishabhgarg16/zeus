package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.Match
import com.hit11.zeus.model.MatchStatus
import com.hit11.zeus.model.TeamEntity
import com.hit11.zeus.model.external.CricbuzzMatchInfo
import com.hit11.zeus.model.external.CricbuzzMatchResponse
import com.hit11.zeus.repository.MatchRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import javax.transaction.Transactional

@Service
class MatchService(
    private val teamService: TeamService,
    private val matchRepository: MatchRepository,
    private val cricbuzzApiService: CricbuzzApiService
) {
    private val logger = Logger.getLogger(this::class.java)
    private val matchExistsCache = ConcurrentHashMap<Int, Match>()

    // Simple cache for matches list with TTL
    data class MatchListCache(
        val matches: List<Match>,
        val expiryTime: Instant
    )

    private var matchListCache: MatchListCache? = null
    private val CACHE_TTL_MINUTES = 2L

    fun getRelevantMatches(limit: Int): List<Match> {
        // 1. Fetch live/upcoming matches from Cricbuzz
        try {
            val cricbuzzMatches = cricbuzzApiService.getLiveAndUpcomingMatches()
            syncCricbuzzMatches(cricbuzzMatches)
        } catch (e: Exception) {
            logger.error("Error fetching/syncing Cricbuzz matches", e)
        }

        // 2. Get matches from DB with priority sorting
        return getMatchesWithPriority(limit)
    }

    private fun getMatchesWithPriority(limit: Int): List<Match> {
        // Check if cache exists and is still valid
        matchListCache?.let { cache ->
            if (cache.expiryTime.isAfter(Instant.now())) {
                logger.info("Returning matches from cache")
                return cache.matches.take(limit)
            }
        }

        // Cache expired or doesn't exist, fetch from DB
        val recentCompletedMatchThreshold = Instant.now().minus(24, ChronoUnit.HOURS)
        val activeStatuses = listOf(
            MatchStatus.SCHEDULED.text,
            MatchStatus.IN_PROGRESS.text,
            MatchStatus.PREVIEW.text,
            MatchStatus.TEA.text,
            MatchStatus.INNINGS_BREAK.text,
            MatchStatus.TOSS.text,
            MatchStatus.DRINK.text,
            MatchStatus.STUMPS.text // if needed
        )

        return try {
            val matches = matchRepository.findMatchesWithTeams(
                activeStatuses = activeStatuses,
                currentTimestamp = Instant.now(),
                recentCompletedMatchThreshold = recentCompletedMatchThreshold,
                pageable = PageRequest.of(0, limit * 2) // Fetch more than needed to allow for priority sorting
            ).sortedBy { match -> getMatchPriority(match) }
                .take(limit)

            // Update cache
            matchListCache = MatchListCache(
                matches = matches,
                expiryTime = Instant.now().plusSeconds(CACHE_TTL_MINUTES * 60)
            )

            matches
        } catch (e: Exception) {
            logger.error("Error fetching matches from DB", e)
            emptyList()
        }
    }

    fun isLiveStatus(status: String): Boolean {
        return status in setOf(
            MatchStatus.IN_PROGRESS.text,
            MatchStatus.INNINGS_BREAK.text,
            MatchStatus.TEA.text,
            MatchStatus.TOSS.text,
            MatchStatus.DRINK.text,
            MatchStatus.STUMPS.text
        )
    }

    private fun getMatchPriority(match: Match): Int {
        val now = Instant.now()
        val isWithinNext6Hours = match.startDate?.isBefore(now.plus(6, ChronoUnit.HOURS)) ?: false
        val isWithinPast6Hours = match.endDate?.isAfter(now.minus(6, ChronoUnit.HOURS)) ?: false

        val isIndiaMatch = match.team1Entity?.teamName?.contains("India", ignoreCase = true) == true ||
                match.team2Entity?.teamName?.contains("India", ignoreCase = true) == true

        val isIPL = match.tournamentName?.contains("Indian Premier League", ignoreCase = true) == true ||
                match.tournamentName?.contains("IPL", ignoreCase = true) == true

        val isLive = isLiveStatus(match.status)

        return when {
            // India Matches Priority
            isIndiaMatch && isLive -> MatchPriority.INDIA_LIVE.value

            // IPL Priority
            isIPL && isLive -> MatchPriority.IPL_LIVE.value

            // Other Live Matches
            isLive -> MatchPriority.OTHER_LIVE.value

            isIndiaMatch && match.status == MatchStatus.SCHEDULED.text && isWithinNext6Hours ->
                MatchPriority.INDIA_UPCOMING_6H.value

            isIndiaMatch && match.status == MatchStatus.COMPLETE.text && isWithinPast6Hours ->
                MatchPriority.INDIA_COMPLETED_6H.value

            isIPL && match.status == MatchStatus.SCHEDULED.text && isWithinNext6Hours ->
                MatchPriority.IPL_UPCOMING_6H.value

            isIPL && match.status == MatchStatus.COMPLETE.text && isWithinPast6Hours ->
                MatchPriority.IPL_COMPLETED_6H.value

            // Other Matches Time-based Priority
            match.status == MatchStatus.SCHEDULED.text && isWithinNext6Hours ->
                MatchPriority.OTHER_UPCOMING_6H.value

            match.status == MatchStatus.COMPLETE.text && isWithinPast6Hours ->
                MatchPriority.OTHER_COMPLETED_6H.value

            // Remaining matches with basic priority
            isIndiaMatch -> MatchPriority.INDIA_OTHER.value
            isIPL -> MatchPriority.IPL_OTHER.value

            else -> MatchPriority.REMAINING.value
        }
    }

    @Transactional
    private fun syncCricbuzzMatches(response: CricbuzzMatchResponse) {
        val existingMatchIds = mutableSetOf<Int>()
        val newMatches = mutableListOf<Match>()
        val updatedMatches = mutableListOf<Match>()

        response.typeMatches.forEach { typeMatch ->
            val matchType = typeMatch.matchType
            typeMatch.seriesMatches.forEach { seriesMatch ->
                seriesMatch.seriesAdWrapper?.matches?.forEach { cricbuzzMatch ->
                    try {
                        val cricbuzzMatchInfo = cricbuzzMatch.matchInfo

                        if (cricbuzzMatchInfo.matchId in existingMatchIds) return@forEach
                        existingMatchIds.add(cricbuzzMatchInfo.matchId)

                        // First ensure teams exist
                        val team1 = teamService.getOrCreateTeam(cricbuzzMatchInfo.team1) ?: return@forEach
                        val team2 = teamService.getOrCreateTeam(cricbuzzMatchInfo.team2) ?: return@forEach

                        val cricbuzzMatchId = cricbuzzMatchInfo.matchId
                        val existingMatch = checkIfMatchExists(cricbuzzMatchId)

                        if (existingMatch != null) {
                            // Update only if there are changes
                            if (hasMatchChanged(existingMatch, cricbuzzMatchInfo)) {
                                val updatedMatch =
                                    updateMatch(existingMatch, cricbuzzMatchInfo, team1, team2)
                                updatedMatches.add(updatedMatch)
                            }
                        } else {
                            // Create new match
                            val newMatch = createMatch(cricbuzzMatchInfo, typeMatch.matchType, team1, team2)
                            newMatches.add(newMatch)
                        }
                    } catch (e: Exception) {
                        logger.error("Error syncing match ${cricbuzzMatch.matchInfo.matchId}", e)
                    }
                }
            }
        }

        try {
            if (newMatches.isNotEmpty() || updatedMatches.isNotEmpty()) {
                // Clear matches cache when any updates happen
                matchListCache = null
                logger.info("Cleared matches cache due to updates")
            }
            // Batch save new matches
            if (newMatches.isNotEmpty()) {
                matchRepository.saveAll(newMatches)
                newMatches.map { matchExistsCache.put(it.cricbuzzMatchId!!, it) }
                logger.info("Created ${newMatches.size} new matches")
            }

            // Batch update existing matches
            if (updatedMatches.isNotEmpty()) {
                matchRepository.saveAll(updatedMatches)
                updatedMatches.map { matchExistsCache.remove(it.cricbuzzMatchId!!) }
                logger.info("Updated ${updatedMatches.size} existing matches")
            }
        } catch (e: Exception) {
            logger.error("Error saving matches to database", e)
            throw e
        }
    }

    private fun checkIfMatchExists(cricbuzzMatchId: Int): Match? {
        return matchExistsCache[cricbuzzMatchId]
            ?: matchRepository.findByCricbuzzMatchId(cricbuzzMatchId)?.also {
                matchExistsCache[cricbuzzMatchId] = it
            }
    }

    private fun hasMatchChanged(existingMatch: Match, matchInfo: CricbuzzMatchInfo): Boolean {
        return existingMatch.status != convertCricbuzzStatus(matchInfo.state) ||
                existingMatch.startDate != Instant.ofEpochMilli(matchInfo.startDate.toLong()) ||
                existingMatch.endDate != Instant.ofEpochMilli(matchInfo.endDate.toLong()) ||
                existingMatch.stadium != matchInfo.venueInfo.ground ||
                existingMatch.city != matchInfo.venueInfo.city
    }

    private fun createMatch(
        cricbuzzMatchInfo: CricbuzzMatchInfo,
        matchType: String,
        team1: TeamEntity,
        team2: TeamEntity
    ): Match {
        return Match(
            cricbuzzMatchId = cricbuzzMatchInfo.matchId,
            team1 = team1.teamName,
            team2 = team2.teamName,
            team1Id = team1.id,
            team2Id = team2.id,
            matchGroup = cricbuzzMatchInfo.seriesName,
            startDate = Instant.ofEpochMilli(cricbuzzMatchInfo.startDate.toLong()),
            endDate = Instant.ofEpochMilli(cricbuzzMatchInfo.endDate.toLong()),
            city = cricbuzzMatchInfo.venueInfo.city,
            stadium = cricbuzzMatchInfo.venueInfo.ground,
            status = convertCricbuzzStatus(cricbuzzMatchInfo.state),
            tournamentName = cricbuzzMatchInfo.seriesName,
            matchFormat = cricbuzzMatchInfo.matchFormat,
            matchType = matchType
        )
    }

    private fun updateMatch(
        existingMatch: Match,
        cricbuzzMatchInfo: CricbuzzMatchInfo,
        team1: TeamEntity,
        team2: TeamEntity,
    ): Match {
        return existingMatch.copy(
            status = convertCricbuzzStatus(cricbuzzMatchInfo.state),
            startDate = Instant.ofEpochMilli(cricbuzzMatchInfo.startDate.toLong()),
            endDate = Instant.ofEpochMilli(cricbuzzMatchInfo.endDate.toLong()),
            city = cricbuzzMatchInfo.venueInfo.city,
            stadium = cricbuzzMatchInfo.venueInfo.ground,
            updatedAt = Instant.now(),
            team1Id = team1.id,
            team2Id = team2.id,
        )
    }

    private fun convertCricbuzzStatus(cricbuzzStatus: String): String {
        return when (cricbuzzStatus.lowercase()) {
            "live" -> MatchStatus.IN_PROGRESS.text
            "in progress" -> MatchStatus.IN_PROGRESS.text
            "complete" -> MatchStatus.COMPLETE.text
            "preview" -> MatchStatus.SCHEDULED.text
            "upcoming" -> MatchStatus.SCHEDULED.text
            "innings break" -> MatchStatus.INNINGS_BREAK.text
            "tea" -> MatchStatus.TEA.text
            "drink" -> MatchStatus.DRINK.text
            "toss" -> MatchStatus.TOSS.text
            "stumps" -> MatchStatus.STUMPS.text
            else -> MatchStatus.IN_PROGRESS.text
        }
    }

    fun getMatchById(matchId: Int): Match? {
        return try {
            matchRepository.findMatchWithTeamsById(matchId)
        } catch (e: Exception) {
            println("Error fetching match by ID: $e")
            null
        }
    }

    private val cache = HashMap<Int, Pair<Instant, Hit11Scorecard>>()
    private fun getScore(cricbuzzMatchId: Int, matchId: Int = 0, useCache: Boolean): Hit11Scorecard? {
        try {
            if (useCache) {
                val cachedScoreCard = cache[cricbuzzMatchId]
                if (cachedScoreCard != null && cachedScoreCard.first > Instant.now().minusSeconds(20)) {
                    return cachedScoreCard.second
                }
            }

            val scoreCard = cricbuzzApiService.getMatchScore(matchId, cricbuzzMatchId)
            if (scoreCard != null) {
                cache[cricbuzzMatchId] = Pair(Instant.now(), scoreCard)
                return scoreCard
            }
        } catch (e: Exception) {
            throw e
        }
        return null
    }

    fun getScoreByMatch(matchId: Int, useCache: Boolean): Hit11Scorecard? {
        val match = getMatchById(matchId)
        return if (match?.cricbuzzMatchId != null) {
            getScore(match.cricbuzzMatchId, match.id, useCache)
        } else {
            null
        }
    }

    fun getScoreByCricbuzzMatch(cricBuzzMatchId: Int, useCache: Boolean): Hit11Scorecard? {
        return getScore(cricBuzzMatchId, useCache = useCache)
    }
}

enum class MatchPriority(val value: Int) {
    INDIA_LIVE(10),           // Live India matches (highest priority)
    IPL_LIVE(20),            // Live IPL matches

    INDIA_UPCOMING_6H(30),    // India matches in next 6 hours
    INDIA_COMPLETED_6H(40),   // India matches completed in last 6 hours

    IPL_UPCOMING_6H(50),     // IPL matches in next 6 hours
    IPL_COMPLETED_6H(60),    // IPL completed in last 6 hours

    OTHER_LIVE(70),          // Other live matches
    OTHER_UPCOMING_6H(80),   // Other matches in next 6 hours
    OTHER_COMPLETED_6H(90),  // Other completed in last 6 hours
    INDIA_OTHER(100),        // Other India matches outside time window
    IPL_OTHER(110),         // Other IPL matches outside time window
    REMAINING(120)          // All other matches
}

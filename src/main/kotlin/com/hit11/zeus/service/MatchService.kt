package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.Match
import com.hit11.zeus.model.MatchStatus
import com.hit11.zeus.repository.MatchRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class MatchService(
    private val matchRepository: MatchRepository,
    private val cricbuzzMatchService: CricbuzzMatchService,
    private val cricbuzzApiService: CricbuzzApiService
) {
    private val logger = Logger.getLogger(this::class.java)
    // Data class to hold cached match list along with its expiry time
    data class MatchListCache(
        val matches: List<Match>,
        val expiryTime: Instant
    )

    // In-memory cache for the match list
    private var matchListCache: MatchListCache? = null
    private val CACHE_TTL_MINUTES = 2L

    fun getRelevantMatches(limit: Int): List<Match> {
        // 1. Fetch live/upcoming matches from Cricbuzz
        try {
            // call cricbuzz api for sync matches
            cricbuzzMatchService.getLiveAndUpcomingMatches()
        } catch (e: Exception) {
            logger.error("Error fetching/syncing Cricbuzz matches", e)
        }

        // 2. Get matches from DB with priority sorting
        return getMatchesWithPriority(limit)
    }

    private fun getMatchesWithPriority(limit: Int): List<Match> {
        // If cache exists and hasn't expired, return cached matches
        matchListCache?.let { cache ->
            if (cache.expiryTime.isAfter(Instant.now())) {
                logger.info("Returning matches from cache")
                return cache.matches.take(limit)
            }
        }
        // Otherwise, fetch matches from DB
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
                pageable = PageRequest.of(0, limit * 5) // Fetch more than needed to allow for priority sorting
            ).sortedBy { match -> getMatchPriority(match) }
                .take(limit)

            // Update the cache with the fresh match list
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

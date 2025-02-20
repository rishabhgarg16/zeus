package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.Match
import com.hit11.zeus.model.MatchStatus
import com.hit11.zeus.model.TeamEntity
import com.hit11.zeus.model.external.CricbuzzMatchInfo
import com.hit11.zeus.model.external.CricbuzzMatchResponse
import com.hit11.zeus.repository.MatchRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.transaction.Transactional

@Service
class CricbuzzMatchService(
    private val teamService: TeamService,
    private val matchRepository: MatchRepository,
    private val cricbuzzApiService: CricbuzzApiService
) {
    private val logger = Logger.getLogger(this::class.java)
    private val matchExistsCache = ConcurrentHashMap<Int, Match>()

    // Cache for live/upcoming matches
    private data class MatchCache(
        val matches: CricbuzzMatchResponse,
        val expiryTime: Instant = Instant.now().plusSeconds(120) // 2 min cache
    )

    private var matchCache: MatchCache? = null

    fun getLiveAndUpcomingMatches(): CricbuzzMatchResponse {
        // Return from cache if valid
        matchCache?.let {
            if (it.expiryTime.isAfter(Instant.now())) {
                return it.matches
            }
        }

        // Fetch fresh data
        val matches = cricbuzzApiService.getLiveAndUpcomingMatches()

        // Update cache
        matchCache = MatchCache(matches)

        // Process teams and matches
        syncTeamsAndMatches(matches)

        return matches
    }

    @Transactional
    private fun syncTeamsAndMatches(response: CricbuzzMatchResponse) {
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
                matchCache = null
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
}
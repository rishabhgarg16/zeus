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

    // Cache for teams during batch processing
    private val teamBatchCache = ConcurrentHashMap<Int, TeamEntity>()

    // Cache for live/upcoming matches
    private data class MatchCache(
        val matches: CricbuzzMatchResponse,
        val expiryTime: Instant = Instant.now().plusSeconds(120) // 2 min cache
    )

    // Filter for Indian leagues and tournaments
    private val isIndianLeague: (String) -> Boolean = { seriesName ->
        seriesName.contains("IPL", ignoreCase = true) ||
                seriesName.contains("Indian Premier League", ignoreCase = true) ||
                seriesName.contains("Premier League", ignoreCase = true) && seriesName.contains("India", ignoreCase = true) ||
                seriesName.contains("BCCI", ignoreCase = true)
    }

    private var matchCache: MatchCache? = null

    fun getLiveAndUpcomingMatches(): CricbuzzMatchResponse {
        // Return from cache if valid
        matchCache?.let {
            if (it.expiryTime.isAfter(Instant.now())) {
                return it.matches
            }
        }

        // Fetch fresh data
        val allMatches = cricbuzzApiService.getLiveAndUpcomingMatches()

        // Filter matches to include only international and Indian leagues
        val filteredMatches = filterMatchTypes(allMatches)

        // Update cache
        if(filteredMatches.typeMatches.isNotEmpty()) {
            matchCache = MatchCache(filteredMatches)

            // Process teams and matches
            syncTeamsAndMatches(filteredMatches)
        }

        return filteredMatches
    }

    private fun filterMatchTypes(response: CricbuzzMatchResponse): CricbuzzMatchResponse {
        val filteredTypeMatches = response.typeMatches.filter { typeMatch ->
            // Include all international matches
            if (typeMatch.matchType == "International") {
                return@filter true
            }
            // For leagues, check if it's an Indian league
            else if (typeMatch.matchType == "League") {
                val hasIndianLeague = typeMatch.seriesMatches.any { seriesMatch ->
                    seriesMatch.seriesAdWrapper?.seriesName?.let { isIndianLeague(it) } ?: false
                }
                return@filter hasIndianLeague
            }
            // Exclude all other match types
            else {
                return@filter false
            }
        }

        // Filter each series to include only Indian leagues for league match type
        val processedTypeMatches = filteredTypeMatches.map { typeMatch ->
            if (typeMatch.matchType == "League") {
                val filteredSeriesMatches = typeMatch.seriesMatches.filter { seriesMatch ->
                    seriesMatch.seriesAdWrapper?.seriesName?.let { isIndianLeague(it) } ?: false
                }
                typeMatch.copy(seriesMatches = filteredSeriesMatches)
            } else {
                typeMatch
            }
        }

        // Return modified response with filtered matches
        return response.copy(typeMatches = processedTypeMatches)
    }

    @Transactional
    private fun syncTeamsAndMatches(response: CricbuzzMatchResponse) {
        val newMatchBatch = mutableListOf<Match>()
        val updateMatchBatch = mutableListOf<Match>()

        // Extract team IDs for batch loading
        val cricbuzzTeamIds = mutableSetOf<Int>()
        response.typeMatches.forEach { typeMatch ->
            typeMatch.seriesMatches.forEach { seriesMatch ->
                seriesMatch.seriesAdWrapper?.matches?.forEach { match ->
                    cricbuzzTeamIds.add(match.matchInfo.team1.teamId)
                    cricbuzzTeamIds.add(match.matchInfo.team2.teamId)
                }
            }
        }

        // Batch load teams to avoid N+1 queries
        val teams = teamService.getTeamsByCricbuzzIds(cricbuzzTeamIds.toList())

        // Cache loaded teams for quick lookup
        teamBatchCache.clear()
        teams.forEach { team ->
            team.cricbuzzTeamId?.let { teamBatchCache[it] = team }
        }

        // Process only unique matches
        val processedMatchIds = mutableSetOf<Int>()

        response.typeMatches.forEach { typeMatch ->
            val matchType = typeMatch.matchType
            typeMatch.seriesMatches.forEach { seriesMatch ->
                seriesMatch.seriesAdWrapper?.matches?.forEach { cricbuzzMatch ->
                    try {
                        val cricbuzzMatchInfo = cricbuzzMatch.matchInfo
                        val cricbuzzMatchId = cricbuzzMatchInfo.matchId

                        // Skip if already processed
                        if (cricbuzzMatchInfo.matchId in processedMatchIds) return@forEach
                        processedMatchIds.add(cricbuzzMatchInfo.matchId)

                        // Get teams from local cache first
                        val team1 = teamBatchCache[cricbuzzMatchInfo.team1.teamId]
                            ?: teamService.getOrCreateTeam(cricbuzzMatchInfo.team1)
                            ?: return@forEach

                        val team2 = teamBatchCache[cricbuzzMatchInfo.team2.teamId]
                            ?: teamService.getOrCreateTeam(cricbuzzMatchInfo.team2)
                            ?: return@forEach

                        // Check existing match
                        // Check existing match efficiently
                        val existingMatch = matchExistsCache[cricbuzzMatchId]
                            ?: matchRepository.findByCricbuzzMatchIdWithTeams(cricbuzzMatchId)

                        // Update match cache
                        existingMatch?.let { matchExistsCache[cricbuzzMatchId] = it }

                        if (existingMatch != null) {
                            // Update only if there are changes
                            if (hasMatchChanged(existingMatch, cricbuzzMatchInfo)) {
                                val updatedMatch =
                                    updateMatch(existingMatch, cricbuzzMatchInfo, team1, team2)
                                updateMatchBatch.add(updatedMatch)
                            }
                        } else {
                            // Create new match
                            val newMatch = createMatch(cricbuzzMatchInfo, typeMatch.matchType, team1, team2)
                            newMatchBatch.add(newMatch)
                        }
                    } catch (e: Exception) {
                        logger.error("Error syncing match ${cricbuzzMatch.matchInfo.matchId}", e)
                    }
                }
            }
        }

        try {
            if (newMatchBatch.isNotEmpty() || updateMatchBatch.isNotEmpty()) {
                // Clear matches cache when any updates happen
                matchCache = null
                logger.info("Cleared matches cache due to updates")
            }
            // Batch save new matches
            if (newMatchBatch.isNotEmpty()) {
                val savedMatches = matchRepository.saveAll(newMatchBatch)
                savedMatches.map { matchExistsCache.put(it.cricbuzzMatchId!!, it) }
                logger.info("Created ${newMatchBatch.size} new matches")
            }

            // Batch update existing matches
            if (updateMatchBatch.isNotEmpty()) {
                val updatedMatches = matchRepository.saveAll(updateMatchBatch)
                // Clear match cache entries that were updated
                updatedMatches.map { matchExistsCache.remove(it.cricbuzzMatchId!!) }
                logger.info("Updated ${updateMatchBatch.size} existing matches")
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
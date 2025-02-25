package com.hit11.zeus.service

import com.hit11.zeus.model.TeamEntity
import com.hit11.zeus.model.external.CricbuzzTeam
import com.hit11.zeus.repository.TeamRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.transaction.Transactional

@Service
class TeamService(
    private val teamRepository: TeamRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Cache for team entities by cricbuzz ID
    private data class CachedTeam(
        val team: TeamEntity,
        val lastUpdated: Instant = Instant.now()
    )

    // Cache settings
    private val CACHE_TTL_MINUTES = 10L

    // Main team cache
    // criccbuzz id to TeamEntity
    private val teamCache = ConcurrentHashMap<Int, CachedTeam>()

    fun getOrCreateTeam(cricbuzzTeam: CricbuzzTeam): TeamEntity? {
        val now = Instant.now()

        // Check if the team is already cached and valid
        // Check if the team is already cached and valid
        teamCache[cricbuzzTeam.teamId]?.let { cached ->
            if (isTeamCacheValid(cached.lastUpdated)) {
                return cached.team
            }
        }

        // Not in cache or expired: Try to retrieve from DB
        val teamFromDB = try {
            teamRepository.findByCricbuzzTeamId(cricbuzzTeam.teamId)
        } catch (ex: Exception) {
            logger.error("Error fetching team from DB for teamId: ${cricbuzzTeam.teamId}", ex)
            null
        }

        // If found in DB, use it; otherwise, create a new team
        val team = teamFromDB ?: createTeam(cricbuzzTeam)

        // If team is still null, return null
        if (team == null) return null

        // Update caches
        val newCachedTeam = CachedTeam(team, now)
        teamCache[cricbuzzTeam.teamId] = newCachedTeam

        return team
    }

    @Transactional
    fun createTeam(cricbuzzTeam: CricbuzzTeam): TeamEntity? {
        val newTeam = TeamEntity(
            teamName = cricbuzzTeam.teamName,
            teamShortName = cricbuzzTeam.teamSName,
            teamImageUrl = generateTeamImageUrl(cricbuzzTeam.imageId),
            cricbuzzTeamId = cricbuzzTeam.teamId
        )
        return try {
            val savedTeam = teamRepository.save(newTeam)
            logger.info("Created new team with ID: ${savedTeam.id} for cricbuzzTeamId: ${cricbuzzTeam.teamId}")
            savedTeam
        } catch (ex: Exception) {
            logger.error("Error creating team for cricbuzzTeamId: ${cricbuzzTeam.teamId}", ex)
            null
        }
    }

    private fun isTeamCacheValid(timestamp: Instant): Boolean {
        return Instant.now().minus(Duration.ofMinutes(CACHE_TTL_MINUTES)).isBefore(timestamp)
    }

    fun updateTeam(teamId: Long, cricbuzzTeam: CricbuzzTeam): TeamEntity? {
        return teamRepository.findById(teamId).map { existingTeam ->
            val updatedTeam = existingTeam.copy(
                teamName = cricbuzzTeam.teamName,
                teamShortName = cricbuzzTeam.teamSName,
                teamImageUrl = generateTeamImageUrl(cricbuzzTeam.imageId)
            )
            val savedTeam = teamRepository.save(updatedTeam)
            // Update cache entry if available
            savedTeam.cricbuzzTeamId?.let { teamCache[it] = CachedTeam(savedTeam) }
            savedTeam
        }.orElse(null)
    }

    private fun generateTeamImageUrl(imageId: Int?): String? {
        return imageId?.let { "https://cricbuzz-cricket.p.rapidapi.com/img/v1/i1/c$it/i.jpg" }
    }

    @Transactional
    fun getTeamsByCricbuzzIds(cricbuzzTeamIds: List<Int>): List<TeamEntity> {
        if (cricbuzzTeamIds.isEmpty()) return emptyList()

        val now = Instant.now()
        val result = mutableListOf<TeamEntity>()
        val missingIds = mutableListOf<Int>()

        // Check cache first for each team
        cricbuzzTeamIds.forEach { teamId ->
            val cachedTeam = teamCache[teamId]
            if (cachedTeam != null && isTeamCacheValid(cachedTeam.lastUpdated)) {
                result.add(cachedTeam.team)
            } else {
                missingIds.add(teamId)
            }
        }

        // Only fetch teams not found in cache
        if (missingIds.isNotEmpty()) {
            val teamEntities = teamRepository.findAllByCricbuzzTeamIdIn(missingIds)

            // Update caches with fetched teams
            teamEntities.forEach { team ->
                team.cricbuzzTeamId?.let { id ->
                    teamCache[id] = CachedTeam(team, now)
                }
                result.add(team)
            }

            // Create any missing teams
            val foundIds = teamEntities.mapNotNull { it.cricbuzzTeamId }.toSet()
            val stillMissingIds = missingIds.filter { it !in foundIds }

            if (stillMissingIds.isNotEmpty()) {
                logger.info("Teams not found for IDs: $stillMissingIds")
                // Teams would be created when individual getOrCreateTeam calls are made
            }
        }

        return result
    }


    fun getCacheStats(): Map<String, Any> {
        return mapOf(
            "cacheSize" to teamCache.size,
            "oldestEntry" to (teamCache.values.minByOrNull { it.lastUpdated }?.lastUpdated ?: Instant.now()),
            "newestEntry" to (teamCache.values.maxByOrNull { it.lastUpdated }?.lastUpdated ?: Instant.now())
        )
    }

}

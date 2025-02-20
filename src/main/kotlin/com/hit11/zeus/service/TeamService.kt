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


data class CachedTeam(
    val team: TeamEntity,
    val lastUpdated: Instant = Instant.now()
)

@Service
class TeamService(
    private val teamRepository: TeamRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // criccbuzz id to TeamEntity
    private val teamCache = ConcurrentHashMap<Int, CachedTeam>()

    fun getOrCreateTeam(cricbuzzTeam: CricbuzzTeam): TeamEntity? {
        val now = Instant.now()

        // Check if the team is already cached and valid
        teamCache[cricbuzzTeam.teamId]?.let { cached ->
            if (now.isBefore(cached.lastUpdated.plus(Duration.ofMinutes(10)))) {
                return cached.team
            } else {
                // TTL expired, remove the cached entry
                teamCache.remove(cricbuzzTeam.teamId)
            }
        }

        // Not in cache or expired: Try to retrieve from DB or create a new team
        val teamFromDB = try {
            teamRepository.findByCricbuzzTeamId(cricbuzzTeam.teamId)
        } catch (ex: Exception) {
            logger.error("Error fetching team from DB for teamId: ${cricbuzzTeam.teamId}", ex)
            null
        }

        // If found in DB, use it; otherwise, attempt to create a new team.
        val team = teamFromDB ?: createTeam(cricbuzzTeam)

        // If team is still null, return null.
        if (team == null) return null

        // Create a new CachedTeam instance with current timestamp
        val newCachedTeam = CachedTeam(team, now)

        // Insert into the cache in a thread-safe manner. If another thread already inserted a value, use that one.
        val cachedTeam = teamCache.putIfAbsent(cricbuzzTeam.teamId, newCachedTeam)
        return (cachedTeam ?: newCachedTeam).team
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

    fun getCacheStats(): Map<String, Any> {
        return mapOf(
            "cacheSize" to teamCache.size,
            "oldestEntry" to (teamCache.values.minByOrNull { it.lastUpdated }?.lastUpdated ?: Instant.now()),
            "newestEntry" to (teamCache.values.maxByOrNull { it.lastUpdated }?.lastUpdated ?: Instant.now())
        )
    }

}

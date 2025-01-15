package com.hit11.zeus.service

import com.hit11.zeus.model.TeamEntity
import com.hit11.zeus.model.external.CricbuzzTeam
import com.hit11.zeus.repository.TeamRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct
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

    @PostConstruct
    fun initializeCache() {
        logger.info("Initializing team cache...")
        try {
            val teams = teamRepository.findAll()
            teams.forEach { team ->
                team.cricbuzzTeamId?.let { teamCache[team.cricbuzzTeamId] = CachedTeam(team) }
            }
            logger.info("Team cache initialized with ${teams.size} teams")
        } catch (e: Exception) {
            logger.error("Error initializing team cache", e)
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs at midnight every day
    fun refreshCache() {
        logger.info("Starting scheduled team cache refresh")
        try {
            val teams = teamRepository.findAll()
            teams.forEach { team ->
                team.cricbuzzTeamId?.let { teamCache[team.cricbuzzTeamId] = CachedTeam(team) }
            }
            logger.info("Team cache refreshed with ${teams.size} teams")
        } catch (e: Exception) {
            logger.error("Error refreshing team cache", e)
        }
    }

    fun getOrCreateTeam(cricbuzzTeam: CricbuzzTeam): TeamEntity? {
        return teamCache[cricbuzzTeam.teamId]?.team ?: fetchTeamFromDB(cricbuzzTeam)
    }

    fun fetchTeamFromDB(cricbuzzTeam: CricbuzzTeam): TeamEntity? {
        val team = teamRepository.findByCricbuzzTeamId(cricbuzzTeam.teamId)
        if (team == null) {
            val createdTeam = createTeam(cricbuzzTeam)
            createdTeam?.let { teamCache[cricbuzzTeam.teamId] =  CachedTeam(createdTeam) }
            return createdTeam
        } else {
            teamCache[cricbuzzTeam.teamId] = CachedTeam(team)
            return team
        }
    }

    @Transactional
    fun createTeam(cricbuzzTeam: CricbuzzTeam): TeamEntity? {
        val newTeam = TeamEntity(
            teamName = cricbuzzTeam.teamName,
            teamShortName = cricbuzzTeam.teamSName,
            teamImageUrl = generateTeamImageUrl(cricbuzzTeam.imageId),
            cricbuzzTeamId = cricbuzzTeam.teamId
        )

        try {
            return teamRepository.save(newTeam)
        } catch (ex: Exception) {
            logger.error("Error creating team: ${ex.message}")

        }
        return null
    }

    fun updateTeam(teamId: Long, cricbuzzTeam: CricbuzzTeam): TeamEntity? {
        return teamRepository.findById(teamId).map { existingTeam ->
            val updatedTeam = existingTeam.copy(
                teamName = cricbuzzTeam.teamName,
                teamShortName = cricbuzzTeam.teamSName,
                teamImageUrl = generateTeamImageUrl(cricbuzzTeam.imageId)
            )
            teamRepository.save(updatedTeam)
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

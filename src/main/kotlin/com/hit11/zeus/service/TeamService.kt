package com.hit11.zeus.service

import com.google.cloud.firestore.Firestore
import com.hit11.zeus.model.UserTeam
import com.hit11.zeus.repository.TeamRepository
import org.springframework.stereotype.Service

@Service
class TeamService(
    private val firestore: Firestore,
    private val teamRepository: TeamRepository
) {
    private val userTeamCollection = firestore.collection("user_team")

    fun getUserTeams(userId: String, matchId: Int): List<UserTeam> {
        return userTeamCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("matchId", matchId).get().get()
            .toObjects(UserTeam::class.java)
    }

    fun saveUserTeam(userTeam: UserTeam): UserTeam? {
        return userTeamCollection.add(userTeam).get().get().get().toObject(UserTeam::class.java)
    }
}

package com.hit11.zeus.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.Timestamp
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.Query
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.model.Match
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MatchRepository(@Autowired private val objectMapper: ObjectMapper) {

    private val firestore: Firestore = FirestoreClient.getFirestore()

    private var matches: MutableList<Match> = mutableListOf()
    private var lastUpdated: Instant = Instant.now()

    fun getUpcomingMatches(): List<Match> {

        if (Instant.now().isBefore(lastUpdated.plusSeconds(60)) and matches.isNotEmpty()) {
            return matches
        }

        matches.clear()
        lastUpdated = Instant.now()
        try {
            val querySnapshot =
                firestore.collection("fixtures_2").whereGreaterThan("end_date", Instant.now().epochSecond)
                    .orderBy("start_date", Query.Direction.ASCENDING).limit(4).get().get()
            for (document in querySnapshot.documents) {
                val json = document.data
                if (json != null) {
                    try {
                        val match = objectMapper.convertValue(json, Match::class.java)
                        match.docRef = document.reference.path
                        matches.add(match)
                    } catch (e: Exception) {
                        val matchId = json.get("id").toString()
                        println("Error deserializing match : $matchId $e")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
        }
        return matches
    }
}
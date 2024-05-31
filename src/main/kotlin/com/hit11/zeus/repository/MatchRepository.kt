package com.hit11.zeus.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.model.Match
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class MatchRepository(@Autowired private val objectMapper: ObjectMapper) {

    private val firestore: Firestore = FirestoreClient.getFirestore()

    fun getUpcomingMatches(): List<Match> {
        val matches = mutableListOf<Match>()

        try {
            val querySnapshot = firestore.collection("fixtures").get().get()
            for (document in querySnapshot.documents) {
                val json = document.data
                if (json != null) {
                    val match = objectMapper.convertValue(json, Match::class.java)
                    matches.add(match)
                }
            }
        } catch (e: Exception) {
            println("Error fetching upcoming matches: $e")
        }
        return matches
    }
}
package com.hit11.zeus.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.hit11.zeus.model.Match

class MatchDeserializer : JsonDeserializer<Match>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Match {
        val node: JsonNode = p.codec.readTree(p)

        val id = node.get("id").asText().toInt()
        val firebaseId = node.get("firebase_id").asText()
        val matchNumber = node.get("match_number").asText().toIntOrNull() ?: -1
        val matchGroup = node.get("match_group").asTextOrNull()
        val team1 = node.get("team_1").asTextOrNull()
        val team1ImageUrl = node.get("team_1_image_url").asTextOrNull()
        val team2 = node.get("team_2").asTextOrNull()
        val team2ImageUrl = node.get("team_2_image_url").asTextOrNull()
        val timeGmt = node.get("time_gmt").asTextOrNull()
        val city = node.get("city").asTextOrNull()
        val stadium = node.get("stadium").asTextOrNull()
        val country = node.get("country").asTextOrNull()
        val tournamentName = node.get("tournament_name").asTextOrNull()
        val matchType = node.get("match_type").asTextOrNull()
        val matchStatus = node.get("match_status").asTextOrNull()
        val matchLink = node.get("match_link").asTextOrNull()
        val startDate = node.get("start_date").asTextOrNull()
        val uploadedAt = node.get("uploaded_at").asLong()
        val team1ShortName = node.get("team1_short_name").asTextOrNull()
        val team2ShortName = node.get("team2_short_name").asTextOrNull()

        return Match(
            id = id,
            firebase_id = firebaseId,
            match_number = matchNumber,
            match_group = matchGroup,
            team1 = team1,
            team_1_image_url = team1ImageUrl,
            team2 = team2,
            team_2_image_url = team2ImageUrl,
            time_gmt = timeGmt,
            city = city,
            stadium = stadium,
            country = country,
            tournament_name = tournamentName,
            match_type = matchType,
            match_status = matchStatus,
            match_link = matchLink,
            start_date = startDate,
            uploaded_at = uploadedAt,
            team1_short_name = team1ShortName,
            team2_short_name = team2ShortName,
        )
    }

    private fun JsonNode.asTextOrNull(): String? = if (isNull) null else asText()
}

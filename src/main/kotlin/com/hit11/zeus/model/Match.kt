package com.hit11.zeus.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hit11.zeus.config.MatchDeserializer

@JsonDeserialize(using = MatchDeserializer::class)
data class Match(
    val id: Int? = null,  // Human-readable ID
    var docRef: String? = "",
    val firebase_id: String? = null,  // Firebase generated ID
    val match_number: Int? = null,
    val match_group: String? = null,
    val team1: String? = null,
    val team_1_image_url: String? = null,
    val team2: String? = null,
    val team_2_image_url: String? = null,
    val time_gmt: String? = null,
    val city: String? = null,
    val stadium: String? = null,
    val country: String? = null,
    val enabled: Boolean = true,
    val tournament_name: String? = null,
    val match_type: String? = null,
    val match_status: String? = null,
    val match_link: String? = null,
    val start_date: String? = null,
    val uploaded_at: Long? = null,
    val team1_short_name: String? = null,
    val team2_short_name: String? = null
)
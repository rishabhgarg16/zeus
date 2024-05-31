package com.obsidian.warhammer.repository.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OpinionDataModel(
    @JsonProperty("match_id")
    val id: Int,

    @JsonProperty("quiz_details")
    val questionDetail: String,

    @JsonProperty("quiz_text")
    val questionText: String,

    @JsonProperty("option_a")
    val optionA: String,

    @JsonProperty("option_a_wager")
    val optionAWager: Long,

    @JsonProperty("option_b")
    val optionB: String,

    @JsonProperty("option_b_wager")
    val optionBWager: Long,

    @JsonProperty("option_a_users")
    val traderACount: Long,

    @JsonProperty("option_b_users")
    val traderBCount: Long,

    @JsonProperty("tags")
    val category: List<String>,

    @JsonProperty("enabled")
    val enabled: Boolean,

    @JsonProperty("tradersInterested")
    val tradersInterested: Long
)

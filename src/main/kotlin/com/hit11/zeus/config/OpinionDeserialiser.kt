package com.hit11.zeus.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.hit11.zeus.model.OpinionDataModel

class OpinionDataModelDeserializer : JsonDeserializer<OpinionDataModel>() {
override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OpinionDataModel {
    val node: JsonNode = p.codec.readTree(p)

    val id = node.get("match_id").asInt()
    val questionDetail = node.get("quiz_details").asText()
    val questionText = node.get("quiz_text").asText()
    val optionA = node.get("option_a").asText()
    val optionAWager = node.get("option_a_wager").asLong()
    val optionB = node.get("option_b").asText()
    val optionBWager = node.get("option_b_wager").asLong()
    val traderACount = node.get("option_a_users").asLong()
    val traderBCount = node.get("option_b_users").asLong()
    val category = node.get("tags").map { it.asText() }
    val enabled = node.get("enabled").asBoolean()

    return OpinionDataModel(
            id = id,
            questionDetail = questionDetail,
            questionText = questionText,
            optionA = optionA,
            optionAWager = optionAWager,
            optionB = optionB,
            optionBWager = optionBWager,
            traderACount = traderACount,
            traderBCount = traderBCount,
            category = category,
            enabled = enabled,
    )
}
}
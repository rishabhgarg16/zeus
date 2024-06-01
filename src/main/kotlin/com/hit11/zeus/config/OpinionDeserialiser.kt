package com.hit11.zeus.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.hit11.zeus.model.Option
import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.PulseOutcome
import com.hit11.zeus.model.UnitOption

class PulseDataModelDeserializer : JsonDeserializer<PulseDataModel>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PulseDataModel {
        val node: JsonNode = p.codec.readTree(p)

        val id = node.get("match_id").asInt()
        val pulseDetail = node.get("quiz_details").asText()
        val pulseText = node.get("quiz_text").asText()

        // TODO can be moved to separate DS
        val option1 = Option(
            optionUnit = if (node.get("option_a").asText()
                    .equals("Yes", ignoreCase = true)
            ) UnitOption.YES else UnitOption.NO,
            traderCount = node.get("option_a_users").asLong(),
            wager = node.get("option_a_wager").asDouble()
        )
        val option2 = Option(
            optionUnit = if (node.get("option_b").asText()
                    .equals("Yes", ignoreCase = true)
            ) UnitOption.YES else UnitOption.NO,
            traderCount = node.get("option_b_users").asLong(),
            wager = node.get("option_b_wager").asDouble()
        )
        val options: List<Option> = listOf(option1, option2)
        val category = node.get("tags").map { it.asText() }
        val enabled = node.get("enabled").asBoolean()

        // TODO can be moved to separate DS
        val pulseResult = node.get("pulse_outcome").asInt()
        val pulseOutcome = when (pulseResult) {
            PulseOutcome.WON.outcome -> PulseOutcome.WON
            PulseOutcome.LOSE.outcome -> PulseOutcome.LOSE
            PulseOutcome.ACTIVE.outcome -> PulseOutcome.ACTIVE
            else -> throw IllegalArgumentException("Unknown option: $pulseResult")
        }

        return PulseDataModel(
            id = id,
            pulseDetail = pulseDetail,
            pulseText = pulseText,
            options = options,
            category = category,
            enabled = enabled,
            pulseOutcome = pulseOutcome
        )
    }
}
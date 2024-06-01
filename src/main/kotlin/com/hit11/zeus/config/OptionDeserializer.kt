package com.hit11.zeus.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.hit11.zeus.model.Option

//class OptionDeserializer : JsonDeserializer<Option>() {
//    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Option {
//        val node: JsonNode = p.codec.readTree(p)
//        val optionUnit = when(node.get("option_unit").asText()) {
//            "Yes" -> Option.YES
//            "No" -> Option.NO
//            else -> throw IllegalArgumentException("Unknown option: ${node.get("option_unit").asText()}")
//        }
//        val wager = node.get("wager").asDouble()
//        val traderCount = node.get("trader_count").asLong()
//        return Option(optionUnit, wager, traderCount)
//    }
//}

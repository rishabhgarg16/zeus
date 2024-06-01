package com.hit11.zeus.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.hit11.zeus.model.*

//class UserPulseDeserializer : JsonDeserializer<UserPulseDataModel>() {
//    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UserPulseDataModel {
//        val node: JsonNode = p.codec.readTree(p)
//
//        val match_id = node.get("match_id").asInt()
//        val user_id = node.get("user_id").asInt()
//        val pulse_id = node.get("pulse_id").asInt()
//        val answer_time = node.get("answer_time").asLong()
//
//        // TODO can be moved to separate DS
//        val option = node.get("answer_chosen")
//        var option1: Option? = null
//        if (option != null) {
//            val optionUnit = if (option.get("option_unit").asText()
//                    .equals("Yes", ignoreCase = true)
//            ) Option.YES else Option.NO
//            val traderCount = option.get("trader_count").asLong()
//            val wager = option.get("wager").asDouble()
//            option1 = Option(optionUnit = optionUnit, traderCount = traderCount, wager = wager)
//        }
//
//        return UserPulseDataModel(
//            userId = user_id,
//            pulseId = pulse_id,
//            answerChosen = option1,
//            answerTime = answer_time,
//            matchId = match_id
//        )
//    }
//}
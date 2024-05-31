package com.hit11.zeus.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.google.cloud.firestore.annotation.PropertyName
import com.hit11.zeus.config.OpinionDataModelDeserializer

@JsonDeserialize(using = OpinionDataModelDeserializer::class)
class OpinionDataModel(
    var id: Int = 0,
    var questionDetail: String = "",
    var questionText: String = "",
    var optionA: String = "",
    var optionAWager: Long = -1L,
    var optionB: String = "",
    var optionBWager: Long = -1L,
    var traderACount: Long = -1L,
    var traderBCount: Long = -1L,
    var category: List<String> = ArrayList(),
    var enabled: Boolean = false,
    var tradersInterested: Long = -1L
)

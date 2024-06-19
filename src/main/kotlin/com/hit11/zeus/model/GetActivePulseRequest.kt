package com.hit11.zeus.model

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class GetActivePulseRequest(
    @field:NotNull(message = "MatchID cannot be null")
    @field:NotEmpty(message = "MatchIDList cannot be blank")
    val matchIdList: List<Int> = emptyList()
)

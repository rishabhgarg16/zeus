package com.hit11.zeus.model

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

data class GetActivePulseRequest(
    @field:NotNull(message = "MatchID cannot be null")
    @field:Min(value = 1, message = "The value must be positive and integer") val matchId: Int = 0
)

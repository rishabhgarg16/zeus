package com.hit11.zeus.model

import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty

data class GetUserOrderRequest(
    @field:Min(value = 1, message = "The user id must be positive and integer")
    val userId: Int = 0,

    @field:NotEmpty(message = "MatchID List cannot be empty")
    val matchIdList: List<Int> = emptyList(),
)


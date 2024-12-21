package com.hit11.zeus.model.response

import java.math.BigDecimal

data class OrderBookResponse(
    val yesBids: List<Pair<BigDecimal, Long>>,
    val noBids: List<Pair<BigDecimal, Long>>,
    val lastTradedYesPrice: BigDecimal?,
    val lastTradedNoPrice: BigDecimal?,
    val yesVolume: Long,
    val noVolume: Long
)
package com.hit11.zeus.model.response

import java.math.BigDecimal

data class OrderBookResponse(
    val yesBids: List<Pair<BigDecimal, Long>>,  // YES buy orders
    val yesAsks: List<Pair<BigDecimal, Long>>,  // YES sell orders
    val noBids: List<Pair<BigDecimal, Long>>,   // NO buy orders
    val noAsks: List<Pair<BigDecimal, Long>>,   // NO sell orders
    val lastTradedYesPrice: BigDecimal?,
    val lastTradedNoPrice: BigDecimal?,
    val yesVolume: Long,
    val noVolume: Long
)
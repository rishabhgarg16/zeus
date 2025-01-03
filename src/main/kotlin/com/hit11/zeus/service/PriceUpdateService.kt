package com.hit11.zeus.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hit11.zeus.websocket.WebSocketHandler
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PriceUpdateService(
    private val webSocketHandler: WebSocketHandler,
    private val objectMapper: ObjectMapper
) {
    fun broadcastPrice(pulseId: Int, priceUpdate: PriceUpdate) {
        val message = objectMapper.writeValueAsString(mapOf(
            "type" to "PRICE_UPDATE",
            "data" to priceUpdate
        ))
        webSocketHandler.broadcastToAllSessions(message)
    }
}

data class PriceUpdate(
    val pulseId: Int,
    val yesBestPrice: BigDecimal?,
    val noBestPrice: BigDecimal?,
    val lastYesPrice: BigDecimal?,
    val lastNoPrice: BigDecimal?,
    val yesVolume: Long,
    val noVolume: Long,
    val timestamp: Long = System.currentTimeMillis()
)
package com.hit11.zeus.controller

import com.hit11.zeus.model.Trade
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.service.TradeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/trade")
class TradeController(
    private val tradeService: TradeService,
) {
    @GetMapping("/fetch_all/pulse/{pulseId}")
    fun getAllTrades(
        @PathVariable("pulseId") pulseId: Int
    ): ResponseEntity<ApiResponse<List<Trade>>> {

        val response = tradeService.getTradesByPulse(pulseId)
        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = response
            )
        )
    }

    @GetMapping("/fetch_all/match/{matchId}")
    fun getAllTradesByMatch(
        @PathVariable("matchId") matchId: Int
    ): ResponseEntity<ApiResponse<List<Trade>>> {

        val response = tradeService.getTradesByMatch(matchId)
        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = response
            )
        )
    }
}
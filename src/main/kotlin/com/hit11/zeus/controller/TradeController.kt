package com.hit11.zeus.controller

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.Trade
import com.hit11.zeus.model.UiMyTradesResponse
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.service.TradeService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/trades")
class TradeController(
    private val tradeService: TradeService,
) {
    private val logger = Logger.getLogger(TradeController::class.java)

    // Fetch all trades by pulse ID
    @GetMapping("/pulse/{pulseId}")
    fun getAllTradesByPulse(@PathVariable pulseId: Int): ResponseEntity<ApiResponse<List<Trade>>> {
        val trades = tradeService.getTradesByPulse(pulseId)
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), null, "Success", trades))
    }

    // Fetch all trades by match ID
    @GetMapping("/match/{matchId}")
    fun getAllTradesByMatch(@PathVariable matchId: Int): ResponseEntity<ApiResponse<List<Trade>>> {
        val trades = tradeService.getTradesByMatch(matchId)
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), null, "Success", trades))
    }

    // Fetch all trades for a given order
    @GetMapping("/order/{orderId}")
    fun getAllTradesByOrder(@PathVariable orderId: Long): ResponseEntity<ApiResponse<List<Trade>>> {
        val trades = tradeService.getTradesByOrder(orderId)
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), null, "Success", trades))
    }

    // Fetch trades by pulse ID and date range
    @GetMapping("/pulse/{pulseId}/range")
    fun getTradesByPulseAndDateRange(
        @PathVariable pulseId: Int,
        @RequestParam("startDate") startDate: Instant,
        @RequestParam("endDate") endDate: Instant
    ): ResponseEntity<ApiResponse<List<Trade>>> {
        val trades = tradeService.getTradesByPulseAndDateRange(pulseId, startDate, endDate)
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), null, "Success", trades))
    }

    // Fetch recent trades by pulse ID
    @GetMapping("/recent/pulse/{pulseId}")
    fun getRecentTradesByPulse(
        @PathVariable pulseId: Int,
        @RequestParam("limit", defaultValue = "10") limit: Int
    ): ResponseEntity<ApiResponse<List<Trade>>> {
        val trades = tradeService.getRecentTradesByPulse(pulseId, limit)
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), null, "Success", trades))
    }

    @GetMapping("/user/{userId}")
    fun getMyTradesResponse(
        @PathVariable userId: Int,
        @RequestParam("matchIds") matchIds: List<Int>,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<List<UiMyTradesResponse>>> {
        return try {
            val pageable: Pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
            val response = tradeService.getMyTradesResponse(userId, matchIds, pageable)
            return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    internalCode = null,
                    message = "Success",
                    data = response
                )
            )
        } catch (e: Exception) {
            logger.error("Error fetching trades", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    internalCode = "TRADES_FETCH_ERROR",
                    message = e.message ?: "An unexpected error occurred",
                    data = null
                )
            )
        }

    }
}
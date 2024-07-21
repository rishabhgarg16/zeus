package com.hit11.zeus.controller

import com.hit11.zeus.adapter.OrderAdapter
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.model.request.GetTradeRequest
import com.hit11.zeus.model.response.OrderResponse
import com.hit11.zeus.oms.OrderPlaceRequest
import com.hit11.zeus.oms.OrderOrchestrator
import com.hit11.zeus.oms.OrderService
import com.hit11.zeus.oms.TradeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/user")
class OrderController(
    private val orderService: OrderService,
    private val orderOrchestrator: OrderOrchestrator,
    private val tradeService: TradeService
) {

    private val logger = Logger.getLogger(this::class.java)

    @PostMapping("/bookOrder")
    fun submitOrder(
        @Valid @RequestBody request: OrderPlaceRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        logger.info("Received request: $request")
        val dataModel = OrderAdapter.convertToDataModel(request)

        try {
            val processedOrder = orderOrchestrator.processOrder(dataModel)
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    status = HttpStatus.CREATED.value(),
                    internalCode = null,
                    message = "Order booked successfully",
                    data = true
                )
            )
        } catch (e: Exception) {
            logger.error("Error processing order: ${e.message}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    internalCode = null,
                    message = "Error processing order",
                    data = false
                )
            )
        }
    }

    @PostMapping("/trades")
    fun getAllTrades(
        @Valid @RequestBody request: GetTradeRequest
    ): ResponseEntity<ApiResponse<List<OrderResponse>>> {

        val response = tradeService.getAllTradesByUserAndMatch(request.userId, request.matchIdList)
        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = response
            )
        )
    }

    @PostMapping("/user/cancelOrder")
    fun cancelOrder(
        @Valid @RequestBody request: CancelOrderRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        orderService.cancelOrder(request.orderId)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Order cancelled successfully",
                data = true
            )
        )
    }
}

data class CancelOrderRequest(
    val orderId: Int
)

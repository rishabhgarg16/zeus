package com.hit11.zeus.controller

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.Order
import com.hit11.zeus.model.OrderRequest
import com.hit11.zeus.model.UiOrderResponse
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.model.response.OrderBookResponse
import com.hit11.zeus.service.MatchingEngine
import com.hit11.zeus.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/order")
class OrderController(
    private val orderService: OrderService,
    private val matchingEngine: MatchingEngine
) {
    private val logger = Logger.getLogger(this::class.java)

    @PostMapping("/bookOrder")
    fun createOrder(
        @Valid @RequestBody request: OrderRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        logger.info("Received request: $request")

        try {
            val isOrderCreated = orderService.createOrder(request)
            if (isOrderCreated) {
                return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse(
                        status = HttpStatus.CREATED.value(),
                        internalCode = null,
                        message = "Order booked successfully",
                        data = true
                    )
                )
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse(
                        status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        internalCode = null,
                        message = "Error processing order",
                        data = false
                    )
                )
            }
        } catch (e: OrderValidationException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    internalCode = null,
                    message = e.message ?: "Order validation failed",
                    data = false
                )
            )
        } catch (e: Exception) {
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

    @GetMapping("/orderbook/{pulseId}")
    fun getOrderBookDepth(
        @PathVariable pulseId: Int,
        @RequestParam(defaultValue = "5") levels: Int
    ): ResponseEntity<ApiResponse<OrderBookResponse>> {
        logger.info("Fetching order book depth for pulseId: $pulseId with levels: $levels")
        return try {
            val orderBook = matchingEngine.getOrderBook(pulseId)
            return synchronized(orderBook) {
                val result = OrderBookResponse(
                    yesBids = orderBook.getOrderBookDepth(levels).yesBids,
                    noBids = orderBook.getOrderBookDepth(levels).noBids,
                    lastTradedYesPrice = orderBook.getLastTradedPrices().first,
                    lastTradedNoPrice = orderBook.getLastTradedPrices().second,
                    yesVolume = orderBook.getVolumes().first,
                    noVolume = orderBook.getVolumes().second
                )
                ResponseEntity.ok(
                    ApiResponse(
                        status = HttpStatus.OK.value(),
                        message = "Order book depth fetched successfully",
                        internalCode = null,
                        data = result
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching order book depth", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "Error fetching order book depth",
                    internalCode = null,
                    data = null
                )
            )
        }
    }

    @GetMapping("/initialize/orderbook")
    fun initializeOrderBook(): ResponseEntity<Boolean> {
        try {
            orderService.initializeOrderBook()
            return ResponseEntity.ok(true)
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(false)
        }
    }

    @PostMapping("/cancelOrder")
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

    @GetMapping("/open/{pulseId}/{userId}")
    fun getAllPendingOrdersByPulseAndUser(
        @PathVariable pulseId: Int,
        @PathVariable userId: Int
    ): ResponseEntity<ApiResponse<List<Order>>> {
        return try {
            val orders = orderService.getPendingOrdersByUserIdAndPulseId(pulseId, userId)
            ResponseEntity.ok(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    internalCode = null,
                    message = "Order cancelled successfully",
                    data = orders
                )
            )
        } catch (e: Exception) {
            logger.error("Error fetching open orders", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "Error fetching open orders",
                    internalCode = null,
                    data = null
                )
            )
        }
    }

    @GetMapping("/pending")
    fun getPendingOrdersByUserAndMatches(
        @RequestParam userId: Int,
        @RequestParam matchIds: List<Int>
    ): ResponseEntity<ApiResponse<List<UiOrderResponse>>> {
        return try {
            val orders = orderService.getPendingOrdersByUserIdAndMatchIds(userId, matchIds)
            ResponseEntity.ok(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    internalCode = null,
                    message = "Orders fetched successfully",
                    data = orders
                )
            )
        } catch (e: Exception) {
            logger.error("Error fetching pending orders", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "Error fetching pending orders",
                    internalCode = null,
                    data = null
                )
            )
        }
    }
}

data class CancelOrderRequest(
    val orderId: Long = 0
)

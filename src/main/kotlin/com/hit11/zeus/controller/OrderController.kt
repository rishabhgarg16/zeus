package com.hit11.zeus.controller

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.OrderRequest
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api/order")
class OrderController(
    private val orderService: OrderService
) {

    private val logger = Logger.getLogger(this::class.java)

    @PostMapping("/bookOrder")
    fun createOrder(
        @Valid @RequestBody request: OrderRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        logger.info("Received request: $request")

        try {
            val createdOrder = orderService.createOrder(request)
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse(
                    status = HttpStatus.CREATED.value(),
                    internalCode = null,
                    message = "Order booked successfully",
                    data = true
                )
            )
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
}

data class CancelOrderRequest(
    val orderId: Int
)

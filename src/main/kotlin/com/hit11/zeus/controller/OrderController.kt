package com.hit11.zeus.controller

import com.hit11.zeus.adapter.UserPulseAdapter
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.ApiResponse
import com.hit11.zeus.model.GetTradeRequest
import com.hit11.zeus.model.TradeResponse
import com.hit11.zeus.model.OrderPlaceRequest
import com.hit11.zeus.service.PulseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class OrderController(private val service: PulseService) {

    private val logger = Logger.getLogger(this::class.java)

    @PostMapping("/user/bookOrder")
    fun submitOrder(
        @Valid @RequestBody request: OrderPlaceRequest
    ): ResponseEntity<ApiResponse<TradeResponse>> {

        logger.info("Received request: $request")
        val dataModel = UserPulseAdapter.toDataModelNew(request)
        val savedResponse = service.submitTrade(dataModel)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                status = HttpStatus.CREATED.value(),
                internalCode = null,
                message = "Order booked successfully",
                data = savedResponse
            )
        )
    }

    @PostMapping("/user/trades")
    fun getAllTrades(
        @Valid @RequestBody request: GetTradeRequest
    ): ResponseEntity<ApiResponse<List<TradeResponse>>> {

        val response = service.getAllTradesByUserAndMatch(request.userId, request.matchIdList)

        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse(
                status = HttpStatus.OK.value(), internalCode = null, message = "Success", data = response
            )
        )
    }
}

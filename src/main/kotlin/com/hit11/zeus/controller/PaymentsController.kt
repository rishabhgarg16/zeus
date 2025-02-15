package com.hit11.zeus.controller

import com.hit11.zeus.config.UserClaimsContext
import com.hit11.zeus.model.PaymentTransaction
import com.hit11.zeus.model.Question
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.annotation.Autowired
import com.hit11.zeus.service.payment.PaymentService
import com.hit11.zeus.model.payment.Payment
import com.hit11.zeus.model.payment.PaymentStatus
import com.hit11.zeus.model.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/payments")
class PaymentsController @Autowired constructor(private val paymentService: PaymentService) {

    @PostMapping("/initiate")
    fun initiatePayment(
        @RequestParam userId: String,
        @RequestParam amount: Double,
        @RequestParam metadata: String
    ): ResponseEntity<ApiResponse<PaymentTransaction?>> {
        val userClaims = UserClaimsContext.getUserClaims()
        if (userClaims != null) {
            return ResponseEntity.ok(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    internalCode = null,
                    message = "Successfully initiated transaction",
                    data = paymentService.createPayment(userClaims.id, amount, metadata)
                )
            )
        }
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Failed initiating transaction",
                data = null
            )
        )
    }

    @PostMapping("/update-status")
    fun updatePaymentStatus(
        @RequestParam paymentId: String,
        @RequestParam status: PaymentStatus
    ): Payment {
        return paymentService.updatePaymentStatus(paymentId, status)
    }
}
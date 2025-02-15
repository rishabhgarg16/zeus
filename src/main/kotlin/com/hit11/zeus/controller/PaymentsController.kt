package com.hit11.zeus.controller

import com.google.api.Http
import com.hit11.zeus.config.UserClaimsContext
import com.hit11.zeus.model.PaymentTransaction
import com.hit11.zeus.model.TransactionStatus
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.annotation.Autowired
import com.hit11.zeus.service.payment.PaymentService
import com.hit11.zeus.model.payment.Payment
import com.hit11.zeus.model.payment.PaymentStatus
import com.hit11.zeus.model.response.ApiResponse
import com.mysql.cj.x.protobuf.Mysqlx.Ok
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity


data class InitiateTransactionStatus(
    var amount: Double? = null,
    var metadata: String? = null
)
data class UpdateTransactionStatus(
    var transactionId: String? = null,
    var status: TransactionStatus? = null,
    var metadata: String? = null
)

@RestController
@RequestMapping("/api/payments")
class PaymentsController @Autowired constructor(private val paymentService: PaymentService) {

    @PostMapping("/initiate")
    fun initiatePayment(
        @RequestBody initiateTransactionStatus: InitiateTransactionStatus,
    ): ResponseEntity<ApiResponse<PaymentTransaction?>> {
        val userClaims = UserClaimsContext.getUserClaims()
        if (userClaims != null) {
            return ResponseEntity.ok(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    internalCode = null,
                    message = "Successfully initiated transaction",
                    data = paymentService.createPayment(userClaims.phone, initiateTransactionStatus?.amount, initiateTransactionStatus.metadata)
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
        @RequestBody updateTransactionStatus: UpdateTransactionStatus
    ): ResponseEntity<ApiResponse<Int?>> {
        var paymentResponse = paymentService.updatePaymentStatus(updateTransactionStatus.transactionId, updateTransactionStatus.status)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "",
                data = paymentResponse
            )
        )
    }
}
package com.hit11.zeus.controller

import com.hit11.zeus.model.PaymentTransaction
import org.springframework.web.bind.annotation.*
import org.springframework.beans.factory.annotation.Autowired
import com.hit11.zeus.service.payment.PaymentService
import com.hit11.zeus.model.payment.Payment
import com.hit11.zeus.model.payment.PaymentStatus

@RestController
@RequestMapping("/payments")
class PaymentsController @Autowired constructor(private val paymentService: PaymentService) {

    @PostMapping("/initiate")
    fun initiatePayment(
        @RequestParam userId: String,
        @RequestParam amount: Double,
        @RequestParam metadata: String
    ): PaymentTransaction {
        return paymentService.createPayment(userId, amount, metadata)
    }

    @PostMapping("/update-status")
    fun updatePaymentStatus(
        @RequestParam paymentId: String,
        @RequestParam status: PaymentStatus
    ): Payment {
        return paymentService.updatePaymentStatus(paymentId, status)
    }
}
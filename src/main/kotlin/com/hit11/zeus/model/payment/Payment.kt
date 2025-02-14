package com.hit11.zeus.model.payment

data class Payment(
    val userId: String,
    val amount: Double,
    val metadata: String,
    val status: PaymentStatus
)

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}
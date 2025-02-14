package com.hit11.zeus.service.payment

import com.hit11.zeus.model.PaymentTransaction
import com.hit11.zeus.model.payment.Payment
import com.hit11.zeus.model.payment.PaymentStatus
import com.hit11.zeus.repository.PaymentTransactionRepository
import com.hit11.zeus.repository.UserRepository
import com.hit11.zeus.repository.WalletTransactionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class PaymentService(
    private val userRepository: UserRepository,
    private val walletTransactionRepository: WalletTransactionRepository,
    private val paymentTransactionRepository: PaymentTransactionRepository
) {
    fun createPayment(userId: String, amount: Double, metadata: String): PaymentTransaction {
        val user = userRepository.findByPhone(userId) ?: throw IllegalArgumentException("User not found")
        val transactionId = UUID.randomUUID().toString()
        val paymentTransaction = PaymentTransaction(
            userId = userId,
            amount = BigDecimal(amount),
            transactionId = transactionId,
            metadata = metadata,
        )
        val savedTransaction = paymentTransactionRepository.save(paymentTransaction)
        return savedTransaction
    }

    fun updatePaymentStatus(transactionId: String, status: PaymentStatus): Payment {
        // Logic to update the payment status in the database
        return Payment(transactionId, 0.0, "", status)
    }
}
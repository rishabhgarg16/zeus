package com.hit11.zeus.service.payment

import com.hit11.zeus.model.PaymentTransaction
import com.hit11.zeus.model.TransactionStatus
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
    fun createPayment(phone: String, amount: Double?, metadata: String?): PaymentTransaction {
        val user = userRepository.findByPhone(phone) ?: throw IllegalArgumentException("User not found")
        val transactionId = UUID.randomUUID().toString()
        val paymentTransaction = PaymentTransaction(
            userId = user.id,
            amount = BigDecimal(amount?:0.0),
            transactionId = transactionId,
            metadata = metadata,
        )
        val savedTransaction = paymentTransactionRepository.save(paymentTransaction)
        return savedTransaction
    }

    fun updatePaymentStatus(transactionId: String?, status: TransactionStatus?): Int {
        if (transactionId == null || status == null) {
            return 0
        }
        var transactionRow = paymentTransactionRepository.updateStatusByTransactionId(transactionId, status)
        return transactionRow
    }
}




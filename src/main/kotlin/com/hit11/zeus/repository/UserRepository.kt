package com.hit11.zeus.repository

import com.hit11.zeus.model.*
import com.hit11.zeus.model.payment.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Optional

@Repository interface UserRepository : JpaRepository<User, Int> {
    fun findByFirebaseUID(firebaseUID: String): User?
    fun save(user: User): User

    fun findByPhone(phone: String): User?
}

@Repository
interface WalletTransactionRepository : JpaRepository<WalletTransaction, Int> {
    fun findByUserId(userId: Int): List<WalletTransaction>
    fun save(transaction: WalletTransaction): WalletTransaction
    fun findByUserIdAndBalanceType(userId: Int, balanceType: BalanceType): List<WalletTransaction>
    fun findByUserIdAndTypeIn(userId: Int, types: List<TransactionType>): List<WalletTransaction>
}

@Repository
interface PaymentTransactionRepository : JpaRepository<PaymentTransaction, Int> {
    fun findByUserId(userId: Int): List<PaymentTransaction>
    fun save(transaction: PaymentTransaction): PaymentTransaction
    fun findByUserIdAndStatus(userId: Int, status: PaymentStatus): List<PaymentTransaction>

    fun findByUserIdAndTransactionId(userId: Int, transactionId: String): PaymentTransaction?

    fun findByTransactionId(transactionId: String): PaymentTransaction?

    @Transactional
    @Modifying
    @Query("UPDATE PaymentTransaction p SET p.status = :status WHERE p.transactionId = :transactionId")
    fun updateStatusByTransactionId(transactionId: String, status: TransactionStatus): Int

}

@Repository
interface PromotionalCreditRepository : JpaRepository<PromotionalCredit, Int> {
    fun findByUserIdAndStatus(userId: Int, status: PromotionalStatus): List<PromotionalCredit>
    fun findByExpiryDateBeforeAndStatus(date: Instant, status: PromotionalStatus): List<PromotionalCredit>
}
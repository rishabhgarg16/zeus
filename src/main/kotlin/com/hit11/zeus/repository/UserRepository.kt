package com.hit11.zeus.repository

import com.hit11.zeus.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository interface UserRepository : JpaRepository<User, Int> {
    fun findByFirebaseUID(firebaseUID: String): User?
    fun save(user: User): User
}

@Repository
interface WalletTransactionRepository : JpaRepository<WalletTransaction, Int> {
    fun findByUserId(userId: Int): List<WalletTransaction>
    fun findByUserIdAndBalanceType(userId: Int, balanceType: BalanceType): List<WalletTransaction>
    fun findByUserIdAndTypeIn(userId: Int, types: List<TransactionType>): List<WalletTransaction>
}

@Repository
interface PromotionalCreditRepository : JpaRepository<PromotionalCredit, Int> {
    fun findByUserIdAndStatus(userId: Int, status: PromotionalStatus): List<PromotionalCredit>
    fun findByExpiryDateBeforeAndStatus(date: Instant, status: PromotionalStatus): List<PromotionalCredit>
}
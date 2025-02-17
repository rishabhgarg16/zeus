package com.hit11.zeus.service

import com.hit11.zeus.model.WalletTransaction
import com.hit11.zeus.model.WalletTransactionRow
import com.hit11.zeus.repository.WalletTransactionRowRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


@Service
class WalletService(
    private val walletTransactionRepo: WalletTransactionRowRepository,
) {
    private val logger = LoggerFactory.getLogger(WalletService::class.java)

    fun getTransactions(userId: Int, pageable: Pageable): Page<WalletTransactionRow> {
        logger.info("Getting transactions for user $userId")
        return walletTransactionRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable)
    }
}
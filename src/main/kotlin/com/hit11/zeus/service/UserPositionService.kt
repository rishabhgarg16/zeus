package com.hit11.zeus.service

import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository
import com.hit11.zeus.repository.UserPositionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import javax.transaction.Transactional


@Service
class UserPositionService(
    private val userService: UserService,
    private val questionRepository: QuestionRepository,
    private val userPositionRepository: UserPositionRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun updateOrCreateUserPosition(
        userId: Int,
        pulseId: Int,
        matchId: Int,
        side: OrderSide,
        quantityDelta: Long, // Will be negative for sells
        newPrice: BigDecimal
    ) {
        val existingPositions =
            userPositionRepository.findAllByUserIdAndPulseIdAndOrderSideAndStatus(
                userId,
                pulseId,
                side,
                status = PositionStatus.OPEN
            )

        // For sell orders, validate sufficient position exists
        if (quantityDelta < 0) {
            val totalExistingQuantity = existingPositions.sumOf { it.quantity }
            if (totalExistingQuantity < -quantityDelta) { // newQuantity is negative for sells
                throw IllegalArgumentException(
                    "Insufficient position quantity. Available: $totalExistingQuantity, Required: ${-quantityDelta}"
                )
            }
        }

        val mergedPosition = existingPositions.reduceOrNull { acc, position ->
            acc.apply {
                this.quantity += position.quantity
                this.averagePrice = calculateNewAveragePrice(
                    this.quantity,
                    this.averagePrice,
                    position.quantity,
                    position.averagePrice
                )
                this.unrealizedPnl += position.unrealizedPnl
                this.realizedPnl += position.realizedPnl
            }
        } ?: UserPosition(
            userId = userId,
            pulseId = pulseId,
            orderSide = side,
            matchId = matchId,
            quantity = 0,
            averagePrice = BigDecimal.ZERO
        )

        mergedPosition.apply {
            quantity += quantityDelta

            // Only recalculate average price for buys
            // For sells, keep existing average price
            if (quantityDelta > 0) {
                averagePrice = calculateNewAveragePrice(
                    quantity,          // Total quantity after adding new
                    averagePrice,      // Current average price
                    quantityDelta,       // Buy quantity being added
                    newPrice          // Price of new buy
                )
            }

            // If selling, update realized PNL
            if (quantityDelta < 0) {
                val sellValue = newPrice.multiply(BigDecimal.valueOf(-quantityDelta))
                val costBasis = averagePrice.multiply(BigDecimal.valueOf(-quantityDelta))
                realizedPnl = realizedPnl.add(sellValue.subtract(costBasis))
            }

            // Update unrealized PNL for remaining position
            updateUnrealizedPnl(this)
        }

        // If position is fully closed, mark as CLOSED
        if (mergedPosition.quantity == 0L) {
            mergedPosition.status = PositionStatus.CLOSED
            mergedPosition.closeTime = Instant.now()
        }

        userPositionRepository.save(mergedPosition)
    }

    private fun calculateNewAveragePrice(
        oldQuantity: Long,
        oldAvgPrice: BigDecimal,
        newQuantity: Long,
        newPrice: BigDecimal
    ): BigDecimal {
        if (oldQuantity < 0 || newQuantity < 0) {
            throw IllegalArgumentException("Quantities cannot be negative")
        }

        // Handle cases where both quantities are zero
        if (oldQuantity == 0L && newQuantity == 0L) {
            throw IllegalArgumentException("Total quantity cannot be zero")
        }

        // Handle cases where oldQuantity is zero (only newPrice contributes)
        if (oldQuantity == 0L) {
            return newPrice.setScale(2, RoundingMode.HALF_UP)
        }

        // Handle cases where newQuantity is zero (only oldAvgPrice contributes)
        if (newQuantity == 0L) {
            return oldAvgPrice.setScale(2, RoundingMode.HALF_UP)
        }

        // General case where both old and new quantities are non-zero
        val totalQuantity = oldQuantity + newQuantity
        return (oldAvgPrice.multiply(BigDecimal(oldQuantity))
            .add(newPrice.multiply(BigDecimal(newQuantity))))
            .divide(BigDecimal(totalQuantity), 2, RoundingMode.HALF_UP)
    }

    fun updateUnrealizedPnl(position: UserPosition) {
        // TODO change this
//        val currentYesPrice = pulseService.getCurrentYesPrice(position.pulseId)
        val currentNoPrice = BigDecimal.TEN.subtract(BigDecimal(5))

        position.unrealizedPnl = (BigDecimal(10).subtract(position.averagePrice))
            .multiply(BigDecimal(position.quantity))
    }

    @Transactional
    fun closePulsePositions(pulseId: Int, pulseResult: PulseResult) {
        if (pulseResult == PulseResult.UNDECIDED) {
            throw IllegalStateException("Pulse $pulseId result is still undecided")
        }

        val positions = userPositionRepository.findByPulseIdAndStatus(pulseId, PositionStatus.OPEN)

        if (positions.isEmpty()) {
            logger.info("No open positions to close for Pulse $pulseId")
            return
        }

        val updatedPositions = positions.map { position ->
            position.apply {
                position.status = PositionStatus.CLOSED
                position.closeTime = Instant.now()
                position.settledAmount = calculateFinalPayout(position, pulseResult)
            }.also { updatedPosition ->
                try {
                    val isWin = pulseResult == PulseResult.Yes && position.orderSide == OrderSide.Yes ||
                            pulseResult == PulseResult.No && position.orderSide == OrderSide.No

                    userService.updateUserWallet(
                        userId = updatedPosition.userId,
                        amount = updatedPosition.settledAmount ?: BigDecimal.ZERO,
                        isWinning = isWin
                    )
                } catch (e: Exception) {
                    logger.error("Failed to update wallet for user ${updatedPosition.userId}", e)
                    throw RuntimeException("Wallet update failed for user ${updatedPosition.userId}")
                }
            }
        }

        // Batch save updated positions
        userPositionRepository.saveAll(updatedPositions)
        logger.info("Successfully closed ${updatedPositions.size} positions for Pulse $pulseId")
    }

    private fun calculateFinalPayout(position: UserPosition, result: PulseResult): BigDecimal {
        return when (result) {
            PulseResult.Yes -> {
                if (position.orderSide == OrderSide.Yes) {
                    // Winning Yes position
                    (BigDecimal.TEN.subtract(position.averagePrice)).multiply(BigDecimal(position.quantity))
                } else {
                    // Losing No position
                    BigDecimal.ZERO
                }
            }

            PulseResult.No -> {
                if (position.orderSide == OrderSide.No) {
                    // Winning No position
                    (BigDecimal.TEN.subtract(position.averagePrice)).multiply(BigDecimal(position.quantity))
                } else {
                    // Losing Yes position
                    BigDecimal.ZERO
                }
            }

            PulseResult.UNDECIDED -> BigDecimal.ZERO
        }
    }

    fun getPositionsByUserAndPulse(userId: Int, pulseId: Int): List<UserPosition> {
        return userPositionRepository.findByUserIdAndPulseId(userId, pulseId)
            ?: throw IllegalStateException("No position found for user $userId and pulse $pulseId")
    }

    fun getPositionsByPulse(pulseId: Int): List<UserPosition> {
        return userPositionRepository.findByPulseId(pulseId)
    }

    fun getAllUserPositions(userId: Int): List<UserPosition> {
        return userPositionRepository.findByUserId(userId)
    }
}
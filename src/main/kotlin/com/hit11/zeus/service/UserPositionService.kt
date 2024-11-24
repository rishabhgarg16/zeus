package com.hit11.zeus.service

import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.OrderSide
import com.hit11.zeus.model.PositionStatus
import com.hit11.zeus.model.PulseResult
import com.hit11.zeus.model.UserPosition
import com.hit11.zeus.repository.QuestionRepository
import com.hit11.zeus.repository.UserPositionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import javax.transaction.Transactional
import kotlin.jvm.optionals.getOrNull


@Service
class UserPositionService(
    private val userService: UserService,
    private val questionRepository: QuestionRepository,
    private val userPositionRepository: UserPositionRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun updatePosition(
        userId: Int,
        pulseId: Int,
        side: OrderSide,
        quantity: Long,
        price: BigDecimal
    ) {
        val position = userPositionRepository.findByUserIdAndPulseId(userId, pulseId)
            ?: UserPosition(userId = userId, pulseId = pulseId)

        when (side) {
            OrderSide.Yes -> {
                position.yesQuantity += quantity
                position.averageYesPrice = calculateNewAveragePrice(
                    position.yesQuantity,
                    position.averageYesPrice,
                    quantity,
                    price
                )
            }

            OrderSide.No -> {
                position.noQuantity += quantity
                position.averageNoPrice = calculateNewAveragePrice(
                    position.noQuantity,
                    position.averageNoPrice,
                    quantity,
                    price
                )
            }

            OrderSide.UNKNOWN ->
                throw OrderValidationException("Unknown side")
        }

        updateUnrealizedPnl(position)
        userPositionRepository.save(position)
    }

    private fun calculateNewAveragePrice(
        oldQuantity: Long,
        oldAvgPrice: BigDecimal,
        newQuantity: Long,
        newPrice: BigDecimal
    ): BigDecimal {
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

        val yesPnl = (BigDecimal(10).subtract(position.averageYesPrice))
            .multiply(BigDecimal(position.yesQuantity))
        val noPnl = (currentNoPrice.subtract(position.averageNoPrice))
            .multiply(BigDecimal(position.noQuantity))

        position.unrealizedPnl = yesPnl.add(noPnl)
    }

    @Transactional
    fun closePulsePositions(pulseId: Int) {
        val pulse = questionRepository.findById(pulseId).getOrNull()
            ?: throw IllegalStateException("Pulse $pulseId not found")

        if (pulse.pulseResult == PulseResult.UNDECIDED) {
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
                position.settledAmount = calculateFinalPayout(position, pulse.pulseResult)
            }.also { updatedPosition ->
                userService.updateUserWallet(updatedPosition.userId, updatedPosition.settledAmount ?: BigDecimal.ZERO)
            }
        }

        // Batch save updated positions
        userPositionRepository.saveAll(updatedPositions)
        logger.info("Successfully closed ${updatedPositions.size} positions for Pulse $pulseId")
    }

    private fun calculateFinalPayout(position: UserPosition, result: PulseResult): BigDecimal {
        return when (result) {
            PulseResult.Yes -> {
                (BigDecimal.TEN.subtract(position.averageYesPrice)).multiply(BigDecimal(position.yesQuantity))
                    .subtract(
                        (BigDecimal.TEN.subtract(position.averageNoPrice)).multiply(BigDecimal(position.noQuantity))
                    )
            }

            PulseResult.No -> {
                // Payout for 'No' positions: (10 - averageNoPrice) * noQuantity
                // Loss for 'Yes' positions: - (10 - averageYesPrice) * yesQuantity
                (BigDecimal.TEN.subtract(position.averageNoPrice)).multiply(BigDecimal(position.noQuantity))
                    .add( // Adding because Yes position loss is negative
                        position.averageYesPrice.subtract(BigDecimal.TEN).multiply(BigDecimal(position.yesQuantity))
                    )
            }

            PulseResult.UNDECIDED -> BigDecimal.ZERO // No payouts for undecided results
        }
    }

    fun getPosition(userId: Int, pulseId: Int): UserPosition {
        val position = userPositionRepository.findByUserIdAndPulseId(userId, pulseId)
            ?: UserPosition(userId = userId, pulseId = pulseId)
        updateUnrealizedPnl(position)
        return position
    }

    fun getPositionsByPulse(pulseId: Int): List<UserPosition> {
        return userPositionRepository.findByPulseId(pulseId)
    }

    fun getAllUserPositions(userId: Int): List<UserPosition> {
        return userPositionRepository.findByUserId(userId)
    }
}
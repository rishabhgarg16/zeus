package com.hit11.zeus.service

import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.*
import com.hit11.zeus.repository.QuestionRepository
import com.hit11.zeus.repository.UserPositionRepository
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
    @Transactional
    fun updatePosition(userId: Int, pulseId: Int, side: OrderSide, quantity: Long, price: BigDecimal) {
        val position = userPositionRepository.findByUserIdAndPulseId(userId, pulseId)
            ?: UserPosition(userId = userId, pulseId = pulseId)

        when (side) {
            OrderSide.Yes -> {
                position.yesQuantity += quantity
                position.averageYesPrice = calculateNewAveragePrice(
                    position.yesQuantity, position.averageYesPrice,
                    quantity, price
                )
            }

            OrderSide.No -> {
                position.noQuantity += quantity
                position.averageNoPrice = calculateNewAveragePrice(
                    position.noQuantity, position.averageNoPrice,
                    quantity, price
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
        if (pulse != null) {
            val result = pulse.pulseResult
            if (result != PulseResult.UNDECIDED) {
                val positions = userPositionRepository.findByPulseIdAndStatus(pulseId, PositionStatus.OPEN)
                positions.forEach { position ->
                    position.status = PositionStatus.CLOSED
                    position.closeTime = Instant.now()
                    position.finalResult = calculateFinalResult(position, result)
                    userPositionRepository.save(position)
                    userService.updateUserWallet(position.userId, position.finalResult!!)

                }
            }
        }
    }

    private fun calculateFinalResult(position: UserPosition, result: PulseResult): BigDecimal {
        return when (result) {
            PulseResult.Yes -> {
                (BigDecimal.TEN.subtract(position.averageYesPrice)).multiply(BigDecimal(position.yesQuantity))
                    .subtract(
                        (BigDecimal.TEN.subtract(position.averageNoPrice)).multiply(BigDecimal(position.noQuantity))
                    )
            }

            PulseResult.No -> {
                position.averageNoPrice.multiply(BigDecimal(position.noQuantity))
                    .subtract(position.averageYesPrice.multiply(BigDecimal(position.yesQuantity)))
            }

            PulseResult.UNDECIDED -> BigDecimal.ZERO
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
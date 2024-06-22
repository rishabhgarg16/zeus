package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.OrderNotSaveException
import com.hit11.zeus.model.TradeDataModel
import com.hit11.zeus.model.toDataModel
import com.hit11.zeus.repository.OrderRepository
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    private val logger = Logger.getLogger(this.javaClass)

    fun getAllTradesByUserIdAndMatchIdIn(userId: Int, matchIdList: List<Int>): List<TradeDataModel>? {
        val temp = orderRepository.findTradesByUserIdAndMatchIdIn(userId, matchIdList)
        return temp?.map { it.toDataModel() }
    }

    fun saveOrder(response: TradeDataModel): TradeDataModel {
        return try {
            val orderEntity = response.toEntity()
            val savedOrder = orderRepository.save(orderEntity)
            savedOrder.toDataModel()
        } catch (e: Exception) {
            logger.error("Error saving order for user id ${response.userId}", e)
            throw OrderNotSaveException("Not able to save order for User ${response.userId}")
        }
    }

    fun getAllTradesByPulseId(pulseId: Int): List<TradeDataModel>? {
        return orderRepository.findTradesByPulseId(pulseId)?.map { it.toDataModel() }
    }
}
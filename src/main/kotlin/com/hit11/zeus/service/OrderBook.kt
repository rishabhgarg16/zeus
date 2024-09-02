package com.hit11.zeus.service

import com.hit11.zeus.model.Order
import com.hit11.zeus.model.OrderSide
import com.hit11.zeus.model.OrderType
import java.math.BigDecimal
import java.util.*

class OrderBook(val pulseId: Int) {
    private val yesBuyOrders = PriorityQueue(compareByDescending<Order> { it.price }.thenBy { it.createdAt })
    private val yesSellOrders = PriorityQueue(compareBy<Order> { it.price }.thenBy { it.createdAt })
    private val noBuyOrders = PriorityQueue(compareByDescending<Order> { it.price }.thenBy { it.createdAt })
    private val noSellOrders = PriorityQueue(compareBy<Order> { it.price }.thenBy { it.createdAt })

    fun addOrder(order: Order) {
        when (order.orderSide) {
            OrderSide.YES ->
                if (order.orderType == OrderType.BUY) yesBuyOrders.offer(order) else yesSellOrders.offer(order)

            OrderSide.NO ->
                if (order.orderType == OrderType.BUY) noBuyOrders.offer(order) else noSellOrders.offer(order)
        }
    }

    fun matchOrders(): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        matches.addAll(matchDirectOrders(yesBuyOrders, yesSellOrders, OrderSide.YES))
        matches.addAll(matchDirectOrders(noBuyOrders, noSellOrders, OrderSide.NO))
        matches.addAll(matchCrossSideOrders())
        return matches
    }

    fun removeOrder(order: Order) {
        when (order.orderSide) {
            OrderSide.YES -> if (order.orderType == OrderType.BUY) yesBuyOrders.remove(
                order
            ) else yesSellOrders.remove(order)

            OrderSide.NO -> if (order.orderType == OrderType.BUY) noBuyOrders.remove(
                order
            ) else noSellOrders.remove(order)
        }
    }

    private fun matchDirectOrders(
        buyOrders: PriorityQueue<Order>, sellOrders: PriorityQueue<Order>, side: OrderSide
    ): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        while (buyOrders.isNotEmpty() && sellOrders.isNotEmpty()) {
            val buyOrder = buyOrders.peek()
            val sellOrder = sellOrders.peek()

            if (buyOrder.price >= sellOrder.price) {
                val matchedQuantity = minOf(buyOrder.remainingQuantity, sellOrder.remainingQuantity)
                val matchPrice = sellOrder.price  // Price-time priority: use the price of the resting order

                matches.add(MatchResult(buyOrder, sellOrder, matchedQuantity, matchPrice, side))

                buyOrder.remainingQuantity -= matchedQuantity
                sellOrder.remainingQuantity -= matchedQuantity

                if (buyOrder.remainingQuantity == 0L) buyOrders.poll()
                if (sellOrder.remainingQuantity == 0L) sellOrders.poll()
            } else {
                break  // No more matches possible
            }
        }
        return matches
    }

    private fun matchCrossSideOrders(): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        matches.addAll(matchCrossSideBuyOrders())
        matches.addAll(matchCrossSideSellOrders())
        return matches
    }

    private fun matchCrossSideBuyOrders(): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        while (yesBuyOrders.isNotEmpty() && noBuyOrders.isNotEmpty()) {
            val yesBuyOrder = yesBuyOrders.peek()
            val noBuyOrder = noBuyOrders.peek()

            val noSellEquivalentPrice = BigDecimal.TEN.subtract(yesBuyOrder.price)
            if (noBuyOrder.price >= noSellEquivalentPrice) {
                val matchedQuantity = minOf(yesBuyOrder.remainingQuantity, noBuyOrder.remainingQuantity)
                val matchPrice = maxOf(yesBuyOrder.price, BigDecimal.TEN.subtract(noBuyOrder.price))

                matches.add(MatchResult(yesBuyOrder, noBuyOrder, matchedQuantity, matchPrice, OrderSide.YES))

                yesBuyOrder.remainingQuantity -= matchedQuantity
                noBuyOrder.remainingQuantity -= matchedQuantity

                if (yesBuyOrder.remainingQuantity == 0L) yesBuyOrders.poll()
                if (noBuyOrder.remainingQuantity == 0L) noBuyOrders.poll()
            } else {
                break  // No more matches possible
            }
        }
        return matches
    }

    private fun matchCrossSideSellOrders(): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        while (yesSellOrders.isNotEmpty() && noSellOrders.isNotEmpty()) {
            val yesSellOrder = yesSellOrders.peek()
            val noSellOrder = noSellOrders.peek()

            val noBuyEquivalentPrice = BigDecimal.TEN.subtract(yesSellOrder.price)
            if (noSellOrder.price <= noBuyEquivalentPrice) {
                val matchedQuantity = minOf(yesSellOrder.remainingQuantity, noSellOrder.remainingQuantity)
                val matchPrice = minOf(yesSellOrder.price, BigDecimal.TEN.subtract(noSellOrder.price))

                matches.add(MatchResult(noSellOrder, yesSellOrder, matchedQuantity, matchPrice, OrderSide.NO))

                yesSellOrder.remainingQuantity -= matchedQuantity
                noSellOrder.remainingQuantity -= matchedQuantity

                if (yesSellOrder.remainingQuantity == 0L) yesSellOrders.poll()
                if (noSellOrder.remainingQuantity == 0L) noSellOrders.poll()
            } else {
                break  // No more matches possible
            }
        }
        return matches
    }

    fun getOrderBookDepth(depth: Int): OrderBookDepth {
        val yesBids = yesBuyOrders.asSequence().take(depth)
            .groupBy { it.price }
            .mapValues { it.value.sumOf { order -> order.remainingQuantity } }
            .toList().sortedByDescending { it.first }

        val yesAsks = yesSellOrders.asSequence().take(depth)
            .groupBy { it.price }
            .mapValues { it.value.sumOf { order -> order.remainingQuantity } }
            .toList().sortedBy { it.first }

        val noBids = noBuyOrders.asSequence().take(depth)
            .groupBy { it.price }
            .mapValues { it.value.sumOf { order -> order.remainingQuantity } }
            .toList().sortedByDescending { it.first }

        val noAsks = noSellOrders.asSequence().take(depth)
            .groupBy { it.price }
            .mapValues { it.value.sumOf { order -> order.remainingQuantity } }
            .toList().sortedBy { it.first }

        return OrderBookDepth(yesBids, yesAsks, noBids, noAsks)
    }
}

data class MatchResult(
    val buyOrder: Order,
    val sellOrder: Order,
    val quantity: Long,
    val price: BigDecimal,
    val side: OrderSide
)

data class OrderBookDepth(
    val yesBids: List<Pair<BigDecimal, Long>>,
    val yesAsks: List<Pair<BigDecimal, Long>>,
    val noBids: List<Pair<BigDecimal, Long>>,
    val noAsks: List<Pair<BigDecimal, Long>>
)
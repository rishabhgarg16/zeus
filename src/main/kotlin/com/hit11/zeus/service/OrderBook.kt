package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.Order
import com.hit11.zeus.model.OrderSide
import java.math.BigDecimal
import java.util.*

class OrderBook(val pulseId: Int) {
    private val logger = Logger.getLogger(this::class.java)
    private var lastTradedYesPrice: BigDecimal? = null
    private var lastTradedNoPrice: BigDecimal? = null

    private val yesBuyOrders = PriorityQueue(compareByDescending<Order> { it.price }.thenBy { it.createdAt })
    private val noBuyOrders = PriorityQueue(compareByDescending<Order> { it.price }.thenBy { it.createdAt })

    // HashSets to track existing orders in the queues
    private val yesOrderIds = mutableSetOf<Long>()
    private val noOrderIds = mutableSetOf<Long>()

    private fun validatePrice(price: BigDecimal, side: OrderSide): Boolean {
        if (price < BigDecimal("1.0") || price > BigDecimal("9.5")) {
            return false
        }
        return true
    }

    fun findPotentialMatches(order: Order): List<MatchResult> {
        // Create deep copies for matching
        // Use copied orders to avoid side effects
        var tempYesOrders = PriorityQueue(compareByDescending<Order> { it.price }.thenBy { it.createdAt })
        tempYesOrders.addAll(yesBuyOrders.map { it.copy() })

        var tempNoOrders = PriorityQueue(compareByDescending<Order> { it.price }.thenBy { it.createdAt })
        tempNoOrders.addAll(noBuyOrders.map { it.copy() })
        // Use copied orders to avoid side effects

        return findMatchingOrders(tempYesOrders, tempNoOrders)
    }

    private fun findMatchingOrders(
        tempYesOrders: PriorityQueue<Order>,
        tempNoOrders: PriorityQueue<Order>
    ): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()

        while (tempYesOrders.isNotEmpty() && tempNoOrders.isNotEmpty()) {
            val yesBuyOrder = tempYesOrders.peek() // 4
            val noBuyOrder = tempNoOrders.peek() // 6.1

            val (matchYesPrice, matchNoPrice) = if (yesBuyOrder.createdAt < noBuyOrder.createdAt) {
                Pair(BigDecimal.TEN.subtract(noBuyOrder.price), noBuyOrder.price)
            } else {
                Pair(yesBuyOrder.price, BigDecimal.TEN.subtract(yesBuyOrder.price)) // 4,6
            }

            if (yesBuyOrder.price >= matchYesPrice &&
                noBuyOrder.price >= matchNoPrice
//                && yesBuyOrder.userId != noBuyOrder.userId // not match against the same user
            ) {
                val matchedQuantity = minOf(
                    yesBuyOrder.remainingQuantity,
                    noBuyOrder.remainingQuantity
                )

                matches.add(
                    MatchResult(
                        yesOrder = yesBuyOrder.copy(),
                        noOrder = noBuyOrder.copy(),
                        matchedQuantity = matchedQuantity,
                        matchedYesPrice = matchYesPrice,
                        matchedNoPrice = matchNoPrice
                    )
                )

                yesBuyOrder.remainingQuantity -= matchedQuantity
                noBuyOrder.remainingQuantity -= matchedQuantity

                if (yesBuyOrder.remainingQuantity == 0L) tempYesOrders.poll()
                if (noBuyOrder.remainingQuantity == 0L) tempNoOrders.poll()
            } else {
                break
            }
        }

        return matches
    }

    fun confirmMatches(newOrder: Order, matches: List<MatchResult>) {
        matches.forEach { match ->
            // Update the actual orders in the queues
            val yesOrder = yesBuyOrders.find { it.id == match.yesOrder.id }
            val noOrder = noBuyOrders.find { it.id == match.noOrder.id }

            yesOrder?.let {
                match.yesOrder.remainingQuantity -= match.matchedQuantity
                it.remainingQuantity -= match.matchedQuantity
                if (it.remainingQuantity == 0L) {
                    yesBuyOrders.remove(it)
                    yesOrderIds.remove(it.id)
                }
            }

            noOrder?.let {
                // update order in the match as it is a temp copy
                match.noOrder.remainingQuantity -= match.matchedQuantity
                // actual order in the main Order Book
                it.remainingQuantity -= match.matchedQuantity
                // remove order from the main Order Book
                if (it.remainingQuantity == 0L) {
                    noBuyOrders.remove(it)
                    noOrderIds.remove(it.id)
                }
            }

            // Update LTP
            lastTradedYesPrice = match.matchedYesPrice
            lastTradedNoPrice = match.matchedNoPrice
        }
    }

    fun addOrder(order: Order): Boolean {
        if (!validatePrice(order.price, order.orderSide)) {
            throw OrderValidationException("Invalid price: ${order.price}")
        }

        val added = when (order.orderSide) {
            OrderSide.Yes -> addToQueue(order, yesBuyOrders, yesOrderIds)
            OrderSide.No -> addToQueue(order, noBuyOrders, noOrderIds)
            OrderSide.UNKNOWN -> throw OrderValidationException("Unknown order side")
        }

        return added
    }

    private fun addToQueue(
        order: Order,
        queue: PriorityQueue<Order>,
        orderSet: MutableSet<Long>
    ): Boolean {
        // Check if the order is already present in the set
        if (order.id in orderSet) return false

        // Add to queue and set
        queue.offer(order)
        orderSet.add(order.id)
        return true
    }

    fun removeOrder(order: Order) {
        when (order.orderSide) {
            OrderSide.Yes -> removeFromQueue(order, yesBuyOrders, yesOrderIds)
            OrderSide.No -> removeFromQueue(order, noBuyOrders, noOrderIds)
            OrderSide.UNKNOWN -> throw OrderValidationException("Unknown order side")
        }
    }

    private fun removeFromQueue(
        order: Order,
        queue: PriorityQueue<Order>,
        orderSet: MutableSet<Long>
    ) {
        if (order.id in orderSet) {
            queue.remove(order)
            orderSet.remove(order.id)
        }
    }

    // Get order book depth
    fun getOrderBookDepth(levels: Int = 5): OrderBookDepth {
        val yesBids = yesBuyOrders
            .groupBy { it.price }
            .mapValues { it.value.sumOf { order -> order.remainingQuantity } }
            .entries.sortedByDescending { it.key }
            .take(levels)
            .map { Pair(it.key, it.value) }

        val noBids = noBuyOrders
            .groupBy { it.price }
            .mapValues { it.value.sumOf { order -> order.remainingQuantity } }
            .entries.sortedByDescending { it.key }
            .take(levels)
            .map { Pair(it.key, it.value) }

        return OrderBookDepth(yesBids, noBids)
    }

    // Get order book volumes
    fun getVolumes(): Pair<Long, Long> {
        val yesVolume = yesBuyOrders.sumOf { it.remainingQuantity }
        val noVolume = noBuyOrders.sumOf { it.remainingQuantity }
        return Pair(yesVolume, noVolume)
    }

    fun getLastTradedPrices(): Pair<BigDecimal?, BigDecimal?> {
        return Pair(lastTradedYesPrice, lastTradedNoPrice)
    }
}

data class MatchResult(
    val yesOrder: Order,
    val noOrder: Order,
    val matchedQuantity: Long,
    val matchedYesPrice: BigDecimal,
    val matchedNoPrice: BigDecimal
)

data class OrderBookDepth(
    val yesBids: List<Pair<BigDecimal, Long>>,
    val noBids: List<Pair<BigDecimal, Long>>
)
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

//    fun findAllOpenOrders(): List<Order> {
//        orderRepository.findAll()
//    }

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
                if (it.remainingQuantity == 0L) yesBuyOrders.remove(it)
            }

            noOrder?.let {
                match.noOrder.remainingQuantity -= match.matchedQuantity
                it.remainingQuantity -= match.matchedQuantity
                if (it.remainingQuantity == 0L) noBuyOrders.remove(it)
            }

            // Update LTP
            lastTradedYesPrice = match.matchedYesPrice
            lastTradedNoPrice = match.matchedNoPrice
        }

//        // Add remaining quantity of new order if any
//        if (newOrder.remainingQuantity > 0) {
//            addOrder(newOrder)
//        }
    }

    fun addOrder(order: Order): Boolean {
        if (!validatePrice(order.price, order.orderSide)) {
            throw OrderValidationException("Invalid price: ${order.price}")
        }
        when (order.orderSide) {
            OrderSide.Yes -> yesBuyOrders.offer(order)
            OrderSide.No -> noBuyOrders.offer(order)
            OrderSide.UNKNOWN -> throw OrderValidationException("Unknown order side")
        }
        return true
    }

//    fun matchOrders(): List<MatchResult> {
//        val matches = mutableListOf<MatchResult>()
//        matches.addAll(matchCrossSideOrders())
//        return matches
//    }

    fun removeOrder(order: Order) {
        when (order.orderSide) {
            OrderSide.Yes -> yesBuyOrders.remove(order)
            OrderSide.No -> noBuyOrders.remove(order)
            OrderSide.UNKNOWN -> throw OrderValidationException("Unknown order side")
        }
    }

    private fun matchCrossSideOrders(): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        matches.addAll(matchCrossSideBuyOrders())
        return matches
    }

    private fun matchCrossSideBuyOrders(): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()

        while (yesBuyOrders.isNotEmpty() && noBuyOrders.isNotEmpty()) {
            val yesBuyOrder = yesBuyOrders.peek()
            val noBuyOrder = noBuyOrders.peek()

            val (matchYesPrice, matchNoPrice) = if (yesBuyOrder.createdAt < noBuyOrder.createdAt) {
                // YES order came first, gets price improvement
                // If NO order is willing to pay 3.2, YES gets matched at 6.8
                Pair(BigDecimal.TEN.subtract(noBuyOrder.price), noBuyOrder.price)
            } else {
                // NO order came first, gets their price
                // If YES order is willing to pay 7.0, NO gets matched at 3.0
                Pair(yesBuyOrder.price, BigDecimal.TEN.subtract(yesBuyOrder.price))
            }

            // Example:
            // t1: YES bids 7.0 (first order)
            // t2: NO bids 3.2
            // Match at YES=6.8, NO=3.2 (YES gets better price)
            // Validate if orders can match at these prices
            if (yesBuyOrder.price >= matchYesPrice && noBuyOrder.price >= matchNoPrice) {
                val matchedQuantity = minOf(yesBuyOrder.remainingQuantity, noBuyOrder.remainingQuantity)

                matches.add(
                    MatchResult(
                        yesOrder = yesBuyOrder,
                        noOrder = noBuyOrder,
                        matchedQuantity = matchedQuantity,
                        matchedYesPrice = matchYesPrice,
                        matchedNoPrice = matchNoPrice,
                    )
                )


                // Update LTP
                lastTradedYesPrice = matchYesPrice
                lastTradedNoPrice = matchNoPrice

                // Update quantities
                yesBuyOrder.remainingQuantity -= matchedQuantity
                noBuyOrder.remainingQuantity -= matchedQuantity

                // Remove filled orders
                if (yesBuyOrder.remainingQuantity == 0L) yesBuyOrders.poll()
                if (noBuyOrder.remainingQuantity == 0L) noBuyOrders.poll()
            } else {
                break // No match possible at these prices
            }
        }
        return matches
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
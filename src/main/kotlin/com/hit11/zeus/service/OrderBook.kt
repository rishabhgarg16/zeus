package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.Order
import com.hit11.zeus.model.OrderSide
import com.hit11.zeus.model.OrderStatus
import java.math.BigDecimal
import java.util.*

class OrderBook(val pulseId: Int) {
    private val logger = Logger.getLogger(this::class.java)
    private var lastTradedYesPrice: BigDecimal? = null
    private var lastTradedNoPrice: BigDecimal? = null

    // Custom comparator for strict price-time priority
    private val orderComparator = Comparator<Order> { o1, o2 ->
        // First compare by price (descending)
        val priceComparison = o2.price.stripTrailingZeros().compareTo(o1.price.stripTrailingZeros())
        if (priceComparison != 0) {
            priceComparison
        } else {
            // If prices are equal, compare by creation time (ascending)
            o1.createdAt.compareTo(o2.createdAt)
        }
    }

    private val yesBuyOrders = PriorityQueue(orderComparator)
    private val noBuyOrders = PriorityQueue(orderComparator)

    // HashSets to track existing orders in the queues
    private val yesOrderIds = mutableSetOf<Long>()
    private val noOrderIds = mutableSetOf<Long>()

    private fun validatePrice(price: BigDecimal, side: OrderSide): Boolean {
        if (price < BigDecimal("1.0") || price > BigDecimal("9.5")) {
            return false
        }
        return true
    }

    fun findPotentialMatches(order: Order): List<OrderMatch> {
        // Create deep copies for matching
        // Use copied orders to avoid side effects
        var tempYesOrders = PriorityQueue(orderComparator)
        tempYesOrders.addAll(yesBuyOrders.map { it.copy() })

        var tempNoOrders = PriorityQueue(orderComparator)
        tempNoOrders.addAll(noBuyOrders.map { it.copy() })

        return findMatchingOrders(tempYesOrders, tempNoOrders)
    }

    private fun findMatchingOrders(
        tempYesOrders: PriorityQueue<Order>,
        tempNoOrders: PriorityQueue<Order>
    ): List<OrderMatch> {
        val matches = mutableListOf<OrderMatch>()

        while (tempYesOrders.isNotEmpty() && tempNoOrders.isNotEmpty()) {
            val yesBuyOrder = tempYesOrders.peek() // 4
            val noBuyOrder = tempNoOrders.peek() // 6.1

            // Determine match prices based on time priority
            val (matchYesPrice, matchNoPrice) = if (yesBuyOrder.createdAt < noBuyOrder.createdAt) {
                // YES order came first, gets price improvement
                Pair(BigDecimal.TEN.subtract(noBuyOrder.price), noBuyOrder.price)
            } else {
                // NO order came first, gets their price
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
                    OrderMatch(
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

    fun confirmMatches(newOrder: Order, matches: List<OrderMatch>) {
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
            logger.warn("Skipping order ${order.id} with error Invalid price: ${order.price}")
            return false
        }

        // Skip invalid or closed orders
        if (order.status !in listOf(OrderStatus.OPEN, OrderStatus.PARTIALLY_FILLED)) {
            logger.warn("Skipping order ${order.id} with status ${order.status}")
            return false
        }

        val added = when (order.orderSide) {
            OrderSide.Yes -> addToQueue(order, yesBuyOrders, yesOrderIds)
            OrderSide.No -> addToQueue(order, noBuyOrders, noOrderIds)
            OrderSide.UNKNOWN -> {
                logger.error("Unknown order side for order ${order.id}")
                false
            }
        }

        return added
    }

    private fun addToQueue(
        order: Order,
        queue: PriorityQueue<Order>,
        orderSet: MutableSet<Long>
    ): Boolean {
        if (order.id in orderSet) {
            logger.warn("Duplicate order detected: ${order.id}")
            return false
        }

        // Add to queue and set
        queue.offer(order)
        orderSet.add(order.id)
        return true
    }

    fun removeOrder(order: Order) {
        when (order.orderSide) {
            OrderSide.Yes -> {
                yesBuyOrders.remove(order)
                yesOrderIds.remove(order.id)
            }

            OrderSide.No -> {
                noBuyOrders.remove(order)
                noOrderIds.remove(order.id)
            }

            OrderSide.UNKNOWN -> throw OrderValidationException("Unknown order side")
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

data class OrderMatch(
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
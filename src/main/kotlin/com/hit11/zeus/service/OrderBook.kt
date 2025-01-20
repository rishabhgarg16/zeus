package com.hit11.zeus.service

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.OrderValidationException
import com.hit11.zeus.model.Order
import com.hit11.zeus.model.OrderSide
import com.hit11.zeus.model.OrderStatus
import java.math.BigDecimal
import java.util.*

class OrderBook(
    val pulseId: Int,
    private val priceUpdateService: PriceUpdateService
) {
    private val logger = Logger.getLogger(this::class.java)

    private var lastTradedYesPrice: BigDecimal? = null
    private var lastTradedNoPrice: BigDecimal? = null

    // Comparator for Buy Orders: Descending Order (Highest Price First)
    private val buyOrderComparator = Comparator<Order> { o1, o2 ->
        val priceComparison = o2.price.stripTrailingZeros().compareTo(o1.price.stripTrailingZeros())
        if (priceComparison != 0) {
            priceComparison
        } else {
            o1.createdAt.compareTo(o2.createdAt)
        }
    }

    // Comparator for Sell Orders: Ascending Order (Lowest Price First)
    private val sellOrderComparator = Comparator<Order> { o1, o2 ->
        val priceComparison = o1.price.stripTrailingZeros().compareTo(o2.price.stripTrailingZeros())
        if (priceComparison != 0) {
            priceComparison
        } else {
            o1.createdAt.compareTo(o2.createdAt)
        }
    }

    private val yesBuyOrders = PriorityQueue(buyOrderComparator)
    private val yesSellOrders = PriorityQueue(sellOrderComparator)
    private val noBuyOrders = PriorityQueue(buyOrderComparator)
    private val noSellOrders = PriorityQueue(sellOrderComparator)

    // HashSets to track existing orders in the queues
    private val yesSellOrderIds = mutableSetOf<Long>()
    private val yesBuyOrderIds = mutableSetOf<Long>()
    private val noSellOrderIds = mutableSetOf<Long>()
    private val noBuyOrderIds = mutableSetOf<Long>()

    private fun validatePrice(price: BigDecimal, side: OrderSide): Boolean {
        if (price < BigDecimal("1.0") || price > BigDecimal("9.5")) {
            return false
        }
        return true
    }

    fun findPotentialMatches(order: Order): List<OrderMatch> {
        // Create deep copies for matching
        // Use copied orders to avoid side effects
        var tempYesBuyOrders = PriorityQueue(buyOrderComparator)
        tempYesBuyOrders.addAll(yesBuyOrders.map { it.copy() })

        var tempNoBuyOrders = PriorityQueue(buyOrderComparator)
        tempNoBuyOrders.addAll(noBuyOrders.map { it.copy() })

        var tempYesSellOrders = PriorityQueue(yesSellOrders)
        tempYesSellOrders.addAll(yesSellOrders.map { it.copy() })

        var tempNoSellOrders = PriorityQueue(noSellOrders)
        tempNoSellOrders.addAll(noSellOrders.map { it.copy() })


        return when (order.orderSide) {
            OrderSide.Yes -> if (order.isBuyOrder) {
                findMatchesForYesBuyOrder(order.copy(), tempYesSellOrders, tempNoBuyOrders)
            } else {
                findMatchesForYesSellOrder(order.copy(), tempYesBuyOrders)
            }

            OrderSide.No -> if (order.isBuyOrder) {
                findMatchesForNoBuyOrder(order.copy(), tempNoSellOrders, tempYesBuyOrders)
            } else {
                findMatchesForNoSellOrder(order.copy(), tempNoBuyOrders)
            }

            else -> emptyList()
        }
    }

    private fun findMatchesForYesBuyOrder(
        newOrder: Order,
        tempYesSellOrders: PriorityQueue<Order>,
        tempNoBuyOrders: PriorityQueue<Order>
    ): List<OrderMatch> {
        val matches = mutableListOf<OrderMatch>()

        // Step 1: Match with Yes Sell orders
        while (
            newOrder.remainingQuantity > 0 && tempYesSellOrders.isNotEmpty() &&
            tempYesSellOrders.peek().userId != newOrder.userId
        ) {
            val yesSellOrder = tempYesSellOrders.peek()
            val buyerYesPrice = newOrder.price
            val buyerMaxNoPrice = BigDecimal.TEN.subtract(buyerYesPrice)

            /**
            Example Scenario:
            YES Sell Orders (Sorted by lowest price first):
            1. Sell 100 @ ₹6.0
            2. Sell 50  @ ₹6.5
            3. Sell 200 @ ₹7.0

            New Order Comes in:
            YES Buy 150 @ ₹6.2

            Matching Logic:
            - Match 100 @ ₹6.2
            - Remaining 50 won't match as Sell price ₹6.5 > Buy price ₹6.2
             **/

            if (buyerYesPrice >= yesSellOrder.price) {
                val matchedQuantity = minOf(newOrder.remainingQuantity, yesSellOrder.remainingQuantity)

                // Apply time priority
                val (matchedYesPrice, matchedNoPrice) = if (newOrder.createdAt < yesSellOrder.createdAt) {
                    // YES buyer came first - so they get seller price as it lower or equal
                    Pair(BigDecimal.TEN.subtract(yesSellOrder.price), yesSellOrder.price)

                } else {
                    // Yes seller came first - so they get equal or higher buyer price
                    Pair(newOrder.price, BigDecimal.TEN.subtract(newOrder.price))

                }

                matches.add(
                    OrderMatch(
                        yesOrder = newOrder,
                        noOrder = yesSellOrder,
                        matchedQuantity = matchedQuantity,
                        matchedYesPrice = matchedYesPrice,
                        matchedNoPrice = matchedNoPrice
                    )
                )

                newOrder.remainingQuantity -= matchedQuantity
                yesSellOrder.remainingQuantity -= matchedQuantity

                if (yesSellOrder.remainingQuantity == 0L) {
                    tempYesSellOrders.poll()
                }
            } else {
                break
            }
        }

        // Step 2: Match with No Buy orders (equivalent to Yes Sell)
        while (newOrder.remainingQuantity > 0 && tempNoBuyOrders.isNotEmpty()
            && tempNoBuyOrders.peek().userId != newOrder.userId
        ) {
            val noBuyOrder = tempNoBuyOrders.peek()
            val equivalentYesSellPrice = BigDecimal.TEN.subtract(noBuyOrder.price)

            if (newOrder.price <= equivalentYesSellPrice) { // Seller price <= Complementary No Buy price
                val matchedQuantity = minOf(newOrder.remainingQuantity, noBuyOrder.remainingQuantity)

                matches.add(
                    OrderMatch(
                        yesOrder = newOrder,
                        noOrder = noBuyOrder,
                        matchedQuantity = matchedQuantity,
                        matchedYesPrice = newOrder.price,
                        matchedNoPrice = BigDecimal.TEN.subtract(newOrder.price)
                    )
                )

                newOrder.remainingQuantity -= matchedQuantity
                noBuyOrder.remainingQuantity -= matchedQuantity

                if (noBuyOrder.remainingQuantity == 0L) {
                    tempNoBuyOrders.poll()
                }
            } else {
                break
            }
        }

//        // Step 2: Match with No Sell orders (cross-side)
//        while (newOrder.remainingQuantity > 0 && tempNoSellOrders.isNotEmpty()) {
//            val noSellOrder = tempNoSellOrders.peek()
//            val buyerYesPrice = newOrder.price
//            val equivalentNoBuyPrice = BigDecimal.TEN.subtract(buyerYesPrice)
//
//            /**
//            Example Scenario:
//            NO Sell Orders (Sorted by lowest price first):
//            1. Sell 100 @ ₹3.8
//            2. Sell 50  @ ₹4.0
//
//            New Order Comes in:
//            YES Buy 150 @ ₹6.2
//
//            Matching Logic:
//            - Match 100 @ ₹6.2, ₹3.8
//            - Remaining 50 won't match as Sell price ₹4.0 > No price ₹3.8
//             **/
//
//            if (noSellOrder.price <= equivalentNoBuyPrice) {
//                val matchedQuantity = minOf(newOrder.remainingQuantity, noSellOrder.remainingQuantity)
//
//                // Apply time priority
//                val (matchedYesPrice, matchedNoPrice) = if (newOrder.createdAt < noSellOrder.createdAt) {
//                    // YES buyer came first - so they get seller price as it lower or equal
//                    Pair(BigDecimal.TEN.subtract(noSellOrder.price), noSellOrder.price)
//
//                } else {
//                    // NO seller came first - so they get equal or higher buyer price
//                    Pair(newOrder.price, BigDecimal.TEN.subtract(newOrder.price))
//
//                }
//
//                matches.add(
//                    OrderMatch(
//                        yesOrder = newOrder,
//                        noOrder = noSellOrder,
//                        matchedQuantity = matchedQuantity,
//                        matchedYesPrice = matchedYesPrice,
//                        matchedNoPrice = matchedNoPrice
//                    )
//                )
//
//                newOrder.remainingQuantity -= matchedQuantity
//                noSellOrder.remainingQuantity -= matchedQuantity
//
//                if (noSellOrder.remainingQuantity == 0L) {
//                    tempNoSellOrders.poll()
//                }
//            } else {
//                break
//            }
//        }

        return matches
    }

    private fun findMatchesForYesSellOrder(
        newOrder: Order,
        tempYesBuyOrders: PriorityQueue<Order>
    ): List<OrderMatch> {
        val matches = mutableListOf<OrderMatch>()

        // Step 1: Match with Yes Buy orders
        while (newOrder.remainingQuantity > 0 && tempYesBuyOrders.isNotEmpty()
            && tempYesBuyOrders.peek().userId != newOrder.userId
        ) {
            val yesBuyOrder = tempYesBuyOrders.peek()
            val sellerYesPrice = newOrder.price // ex: 6 or more
            val equivalentNoBuyerPrice = BigDecimal.TEN.subtract(sellerYesPrice) // ex: 4 or less buy

            /**
            Example Scenario:
            YES Buy Orders (Sorted by highest price first):
            1. Buy 100 @ ₹6.5 (created at T3)
            2. Buy 50  @ ₹6.3 (created at T2)

            New Order Comes in:
            YES Sell 120 @ ₹6.2 (created at T1)

            Matching Logic:
            - First 100 matched @ ₹6.2 (favor seller, as they created the order earlier).
            - Remaining 20 matched @ ₹6.3.
             **/

            // Check if buy price >= sell price

            if (yesBuyOrder.price >= sellerYesPrice) {
                val matchedQuantity = minOf(newOrder.remainingQuantity, yesBuyOrder.remainingQuantity)

                val matchedYesPrice = if (newOrder.createdAt < yesBuyOrder.createdAt) {
                    sellerYesPrice // Favor new order ie. seller if created earlier
                } else {
                    yesBuyOrder.price // Favor Buyer otherwise who is in the Q
                }

                matches.add(
                    OrderMatch(
                        yesOrder = yesBuyOrder,
                        noOrder = newOrder,
                        matchedQuantity = matchedQuantity,
                        matchedYesPrice = matchedYesPrice,
                        matchedNoPrice = BigDecimal.TEN.subtract(matchedYesPrice) // Adjust No Price accordingly
                    )
                )

                yesBuyOrder.remainingQuantity -= matchedQuantity
                newOrder.remainingQuantity -= matchedQuantity

                if (yesBuyOrder.remainingQuantity == 0L) {
                    tempYesBuyOrders.poll()
                }
            } else {
                break
            }
        }
        // If order not fully filled, it will be added to YES SELL order book

        return matches
    }


    private fun findMatchesForNoBuyOrder(
        newNoBuyOrder: Order,
        tempNoSellOrders: PriorityQueue<Order>,
        tempYesBuyOrders: PriorityQueue<Order>
    ): List<OrderMatch> {
        val matches = mutableListOf<OrderMatch>()

        // Step 1: Match with No Sell orders
        while (newNoBuyOrder.remainingQuantity > 0 && tempNoSellOrders.isNotEmpty()
            && tempNoSellOrders.peek().userId != newNoBuyOrder.userId
        ) {
            val noSellOrder = tempNoSellOrders.peek()
            val buyerNoPrice = newNoBuyOrder.price
            val sellerYesMaxPrice = BigDecimal.TEN.subtract(buyerNoPrice)

            /**
            Example Scenario:
            NO Sell Orders (Sorted by lowest price first):
            1. Sell 100 @ ₹4.0
            2. Sell 50  @ ₹4.2

            New Order Comes in:
            NO Buy 120 @ ₹4.2

            Matching Logic:
            - Match 100 @ ₹4.2 (favor seller if created earlier)
            - Remaining 20 @ ₹4.2
             **/

            if (buyerNoPrice >= noSellOrder.price) {
                val matchedQuantity = minOf(newNoBuyOrder.remainingQuantity, noSellOrder.remainingQuantity)

                val matchedNoPrice = if (newNoBuyOrder.createdAt < noSellOrder.createdAt) {
                    noSellOrder.price // Favor Buyer if created earlier as buyer would get lower price
                } else {
                    buyerNoPrice // Favor Seller otherwise as seller wpould get higher price
                }

                matches.add(
                    OrderMatch(
                        yesOrder = noSellOrder,
                        noOrder = newNoBuyOrder,
                        matchedQuantity = matchedQuantity,
                        matchedYesPrice = BigDecimal.TEN.subtract(matchedNoPrice),
                        matchedNoPrice = matchedNoPrice
                    )
                )

                newNoBuyOrder.remainingQuantity -= matchedQuantity
                noSellOrder.remainingQuantity -= matchedQuantity

                if (noSellOrder.remainingQuantity == 0L) {
                    tempNoSellOrders.poll()
                }
            } else {
                break
            }
        }

        // Step 2: Match with Yes Buy orders (complementary beliefs)
        while (newNoBuyOrder.remainingQuantity > 0 && tempYesBuyOrders.isNotEmpty()
            && newNoBuyOrder.userId != tempYesBuyOrders.peek().userId
        ) {
            val yesBuyOrder = tempYesBuyOrders.peek()
            val buyerNoPrice = newNoBuyOrder.price
            val buyerYesPrice = BigDecimal.TEN.subtract(buyerNoPrice)

            if (yesBuyOrder.price >= buyerYesPrice) {
                val matchedQuantity = minOf(newNoBuyOrder.remainingQuantity, yesBuyOrder.remainingQuantity)

                val matchedYesPrice = if (yesBuyOrder.createdAt < newNoBuyOrder.createdAt) {
                    buyerYesPrice // buyerYesPrice is less than yesBuyOrder
                } else {
                    yesBuyOrder.price
                }

                matches.add(
                    OrderMatch(
                        yesOrder = yesBuyOrder,
                        noOrder = newNoBuyOrder,
                        matchedQuantity = matchedQuantity,
                        matchedYesPrice = matchedYesPrice,
                        matchedNoPrice = BigDecimal.TEN.subtract(matchedYesPrice)
                    )
                )

                newNoBuyOrder.remainingQuantity -= matchedQuantity
                yesBuyOrder.remainingQuantity -= matchedQuantity

                if (yesBuyOrder.remainingQuantity == 0L) {
                    tempYesBuyOrders.poll()
                }
            } else {
                break
            }
        }

        return matches
    }

    private fun findMatchesForNoSellOrder(
        newOrder: Order,
        tempNoBuyOrders: PriorityQueue<Order>
    ): List<OrderMatch> {
        val matches = mutableListOf<OrderMatch>()

        // Step 1: Match with No Buy orders
        while (
            newOrder.remainingQuantity > 0 && tempNoBuyOrders.isNotEmpty()
            && tempNoBuyOrders.peek().userId != newOrder.userId
        ) {
            val noBuyOrder = tempNoBuyOrders.peek()
            val sellerNoPrice = newOrder.price
            val buyerYesPrice = BigDecimal.TEN.subtract(sellerNoPrice)

            /**
            Example Scenario:
            NO Buy Orders (Sorted by highest price first):
            1. Buy 100 @ ₹4.2 (created at T1)
            2. Buy 50  @ ₹4.0 (created at T2)

            New Order Comes in:
            NO Sell 120 @ ₹4.0 (created at T3)

            Matching Logic:
            - First 100 matched @ ₹4.0 (favor buyer, as they created the order earlier).
            - Remaining 20 matched @ ₹4.0.
             **/

            if (noBuyOrder.price >= sellerNoPrice) {
                val matchedQuantity = minOf(newOrder.remainingQuantity, noBuyOrder.remainingQuantity)

                val matchedNoPrice = if (noBuyOrder.createdAt < newOrder.createdAt) {
                    sellerNoPrice // Favor Buyer: Match at Seller's price as its equal or less than buy
                } else {
                    noBuyOrder.price // Favor Seller: Match at Buyer's price
                }

                matches.add(
                    OrderMatch(
                        yesOrder = newOrder,
                        noOrder = noBuyOrder,
                        matchedQuantity = matchedQuantity,
                        matchedYesPrice = BigDecimal.TEN.subtract(matchedNoPrice), // Compute Yes Price
                        matchedNoPrice = matchedNoPrice // Final No Price
                    )
                )

                noBuyOrder.remainingQuantity -= matchedQuantity
                newOrder.remainingQuantity -= matchedQuantity

                if (noBuyOrder.remainingQuantity == 0L) {
                    tempNoBuyOrders.poll()
                }
            } else {
                break
            }
        }

        return matches
    }


    fun confirmMatches(newOrder: Order, matches: List<OrderMatch>) {
        matches.forEach { match ->
            // Find the actual orders in main order books
            val yesOrder = when {
                match.yesOrder.isBuyOrder -> yesBuyOrders.find { it.id == match.yesOrder.id }
                else -> yesSellOrders.find { it.id == match.yesOrder.id }
            }

            val noOrder = when {
                match.noOrder.isBuyOrder -> noBuyOrders.find { it.id == match.noOrder.id }
                else -> noSellOrders.find { it.id == match.noOrder.id }
            }

            // Update quantities and remove completed orders
            yesOrder?.let {
                it.remainingQuantity -= match.matchedQuantity
                if (it.remainingQuantity == 0L) {
                    if (it.isBuyOrder) {
                        yesBuyOrders.remove(it)
                        yesBuyOrderIds.remove(it.id)
                    } else {
                        yesSellOrders.remove(it)
                        yesSellOrderIds.remove(it.id)
                    }

                }
            }

            noOrder?.let {
                it.remainingQuantity -= match.matchedQuantity
                if (it.remainingQuantity == 0L) {
                    if (it.isBuyOrder) {
                        noBuyOrders.remove(it)
                        noBuyOrderIds.remove(it.id)
                    } else {
                        noSellOrders.remove(it)
                        noSellOrderIds.remove(it.id)
                    }

                }
            }

            // Update last traded prices
            lastTradedYesPrice = match.matchedYesPrice
            lastTradedNoPrice = match.matchedNoPrice
        }

        // After all matches confirmed, send price update
        sendPriceUpdate()
    }


    private fun sendPriceUpdate() {
        val yesBest = yesBuyOrders.peek()?.price ?: yesSellOrders.peek()?.price
        val noBest = noBuyOrders.peek()?.price ?: noSellOrders.peek()?.price
        val (yesVol, noVol) = getVolumes()

        priceUpdateService.broadcastPrice(
            pulseId,
            PriceUpdate(
                pulseId = pulseId,
                yesBestPrice = yesBest,
                noBestPrice = noBest,
                lastYesPrice = lastTradedYesPrice,
                lastNoPrice = lastTradedNoPrice,
                yesVolume = yesVol,
                noVolume = noVol
            )
        )
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

        val added = when {
            order.orderSide == OrderSide.Yes && order.isBuyOrder ->
                addToQueue(order, yesBuyOrders, yesBuyOrderIds)

            order.orderSide == OrderSide.Yes && !order.isBuyOrder ->
                addToQueue(order, yesSellOrders, yesSellOrderIds)

            order.orderSide == OrderSide.No && order.isBuyOrder ->
                addToQueue(order, noBuyOrders, noBuyOrderIds)

            order.orderSide == OrderSide.No && !order.isBuyOrder ->
                addToQueue(order, noSellOrders, noSellOrderIds)

            else -> {
                logger.error("Unknown order side/type for order ${order.id}")
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
        when {
            order.orderSide == OrderSide.Yes && order.isBuyOrder -> {
                yesBuyOrders.remove(order)
                yesBuyOrderIds.remove(order.id)
            }

            order.orderSide == OrderSide.Yes && !order.isBuyOrder -> {
                yesSellOrders.remove(order)
                yesSellOrderIds.remove(order.id)
            }

            order.orderSide == OrderSide.No && order.isBuyOrder -> {
                noBuyOrders.remove(order)
                noBuyOrderIds.remove(order.id)
            }

            order.orderSide == OrderSide.No && !order.isBuyOrder -> {
                noSellOrders.remove(order)
                noSellOrderIds.remove(order.id)
            }

            else -> throw OrderValidationException("Unknown order side/type")
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

        val yesAsks = yesSellOrders
            .groupBy { it.price }
            .mapValues { it.value.sumOf { order -> order.remainingQuantity } }
            .entries.sortedBy { it.key }
            .take(levels)
            .map { Pair(it.key, it.value) }

        val noBids = noBuyOrders
            .groupBy { it.price }
            .mapValues { it.value.sumOf { order -> order.remainingQuantity } }
            .entries.sortedByDescending { it.key }
            .take(levels)
            .map { Pair(it.key, it.value) }

        val noAsks = noSellOrders
            .groupBy { it.price }
            .mapValues { it.value.sumOf { order -> order.remainingQuantity } }
            .entries.sortedBy { it.key }
            .take(levels)
            .map { Pair(it.key, it.value) }

        return OrderBookDepth(yesBids, yesAsks, noBids, noAsks)
    }

    // Get order book volumes
    fun getVolumes(): Pair<Long, Long> {
        val yesVolume = yesBuyOrders.sumOf { it.remainingQuantity } + yesSellOrders.sumOf { it.remainingQuantity }
        val noVolume = noBuyOrders.sumOf { it.remainingQuantity } + noSellOrders.sumOf { it.remainingQuantity }
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
    val yesAsks: List<Pair<BigDecimal, Long>>,
    val noBids: List<Pair<BigDecimal, Long>>,
    val noAsks: List<Pair<BigDecimal, Long>>
)
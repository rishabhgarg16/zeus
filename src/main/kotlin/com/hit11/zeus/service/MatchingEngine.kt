package com.hit11.zeus.service

import com.hit11.zeus.model.Order
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class MatchingEngine(
    private val priceUpdateService: PriceUpdateService
) {

    private val orderBooks = ConcurrentHashMap<Int, OrderBook>()

    fun getOrderBook(pulseId: Int): OrderBook {
        return orderBooks.computeIfAbsent(pulseId) { OrderBook(it, priceUpdateService) }
    }

    // Find potential matches without modifying order book
    fun findMatches(order: Order): List<OrderMatch> {
        val orderBook = orderBooks.computeIfAbsent(order.pulseId) { OrderBook(it, priceUpdateService) }
        return synchronized(orderBook) {
            orderBook.findPotentialMatches(order)
        }
    }

    // Only modify order book after trades are confirmed
    fun confirmMatches(order: Order, matches: List<OrderMatch>) {
        val orderBook = orderBooks[order.pulseId]
        orderBook?.let {
            synchronized(it) {
                it.confirmMatches(order, matches)
            }
        }
    }

    // Add order to book
    fun addOrder(order: Order): Boolean {
        val orderBook = orderBooks.computeIfAbsent(order.pulseId) { OrderBook(it, priceUpdateService) }
        synchronized(orderBook) {
            return orderBook.addOrder(order)
        }
    }

    fun getOrderBookDepth(pulseId: Int, levels: Int): OrderBookDepth {
        val orderBook = orderBooks.computeIfAbsent(pulseId) { OrderBook(it, priceUpdateService) }
        synchronized(orderBook) {
            return orderBook.getOrderBookDepth(levels)
        }
    }

    fun cancelOrder(order: Order) {
        val orderBook = orderBooks[order.pulseId]
        orderBook?.let {
            synchronized(it) {
                it.removeOrder(order)
            }
        }
    }
}
package com.hit11.zeus.service

import com.hit11.zeus.model.Order
import com.hit11.zeus.service.OrderBook
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class MatchingEngine {
    // Map<pulse id,orderbook>
    private val orderBooks = ConcurrentHashMap<Int, OrderBook>()

    fun processOrder(order: Order): List<MatchResult> {
        val orderBook = orderBooks.computeIfAbsent(order.pulseId) { OrderBook(it) }
        
        synchronized(orderBook) {
            orderBook.addOrder(order)
            return orderBook.matchOrders()
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

//    fun getOrderBookDepth(pulseId: Int, depth: Int): OrderBook.OrderBookDepth? {
//        return orderBooks[pulseId]?.getOrderBookDepth(depth)
//    }
}
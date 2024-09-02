//package com.hit11.zeus.service
//
//import com.hit11.zeus.model.Order
//import com.hit11.zeus.model.Trade
//import org.springframework.data.redis.core.StringRedisTemplate
//
//
//data class OrderMatchResult(val trades: List<Trade>, val remainingOrder: Order?)
//
//class OrderMatchingEngine(private val redisTemplate: StringRedisTemplate) {
//    fun processOrder(order: Order): OrderMatchResult {
//        val orderBook = getOrderBook(order.pulseId)
//        return orderBook.matchOrder(order)
//    }
//
//    fun addToOrderBook(order: Order) {
//        val orderBook = getOrderBook(order.pulseId)
//        orderBook.addOrder(order)
//        saveOrderBook(order.pulseId, orderBook)
//    }
//
//    private fun getOrderBook(pulseId: Int): OrderBook {
//        val serializedOrderBook = redisTemplate.opsForValue().get("orderbook:$pulseId")
//        return if (serializedOrderBook != null) {
//            // Deserialize the order book from Redis
//            deserializeOrderBook(serializedOrderBook)
//        } else {
//            OrderBook(pulseId)
//        }
//    }
//
//    private fun saveOrderBook(pulseId: Int, orderBook: OrderBook) {
//        val serializedOrderBook = serializeOrderBook(orderBook)
//        redisTemplate.opsForValue().set("orderbook:$pulseId", serializedOrderBook)
//    }
//
//    private fun serializeOrderBook(orderBook: OrderBook): String {
//        // Implement serialization logic (e.g., using JSON)
//        return "serialized_order_book"
//    }
//
//    private fun deserializeOrderBook(serialized: String): OrderBook {
//        // Implement deserialization logic
//        return OrderBook(0)
//    }
//}
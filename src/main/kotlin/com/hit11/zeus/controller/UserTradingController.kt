//package com.hit11.zeus.controller
//
//import org.springframework.http.ResponseEntity
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RequestParam
//import org.springframework.web.bind.annotation.RestController
//
//@RestController
//@RequestMapping("/api/user/trading")
//class UserTradingController(
//    private val userOrderService: UserOrderService,
//    private val userTradeService: UserTradeService
//) {
//    @GetMapping("/orders")
//    fun getUserOrders(@RequestParam userId: Int): ResponseEntity<List<OrderStatusDTO>> {
//        val orders = userOrderService.getUserOrderStatus(userId)
//        return ResponseEntity.ok(orders)
//    }
//
//    @GetMapping("/trades")
//    fun getUserTrades(@RequestParam userId: Int): ResponseEntity<List<TradeHistoryDTO>> {
//        val trades = userTradeService.getUserTradeHistory(userId)
//        return ResponseEntity.ok(trades)
//    }
//}
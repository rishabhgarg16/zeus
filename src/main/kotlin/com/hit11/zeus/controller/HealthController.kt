package com.hit11.zeus.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/.well-known")
class HealthController {

    @GetMapping("/live")
    fun healthCheck(): String {
        return "OK"
    }

    @GetMapping("/ready")
    fun readyCheck(): String {
        return "OK"
    }
}
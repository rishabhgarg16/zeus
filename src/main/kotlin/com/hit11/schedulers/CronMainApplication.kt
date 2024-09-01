package com.hit11.schedulers

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
class CronMainApplication {
    fun main() {
        SpringApplication.run(CronMainApplication::class.java)
    }
}
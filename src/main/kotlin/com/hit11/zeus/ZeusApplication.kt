package com.hit11.zeus

import com.hit11.zeus.config.AwsProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(AwsProperties::class)
@EnableScheduling
class ZeusApplication {
    private val logger = LoggerFactory.getLogger(ZeusApplication::class.java)
    companion object {
        private const val TAG = "ZeusApplication"
    }
}

fun main(args: Array<String>) {
    runApplication<ZeusApplication>(*args)
}
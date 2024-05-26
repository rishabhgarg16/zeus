package com.hit11.zeus

import com.google.firebase.FirebaseApp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class ZeusApplication

fun main(args: Array<String>) {
    runApplication<ZeusApplication>(*args)
}

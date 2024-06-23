package com.hit11.zeus

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import javax.annotation.PostConstruct
import java.io.IOException

@SpringBootApplication
class ZeusApplication {

    private val logger = LoggerFactory.getLogger(ZeusApplication::class.java)

    @PostConstruct
    fun initFirebase() {
        try {
            val myObject = object {}
            val serviceAccount = myObject.javaClass.getResourceAsStream("/firebase-sa.json")
                    ?: throw IOException("Firebase service account key file not found")

            val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://default.firebaseio.com")
                    .build()

            FirebaseApp.initializeApp(options)
            logger.info("Firebase initialized successfully with service account: $serviceAccount")
        } catch (e: IOException) {
            logger.error("Error initializing Firebase", e)
            throw RuntimeException("Firebase initialization failed", e)
        }
    }
}

fun main(args: Array<String>) {
    runApplication<ZeusApplication>(*args)
}
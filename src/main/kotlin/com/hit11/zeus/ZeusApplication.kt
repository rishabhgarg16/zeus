package com.hit11.zeus

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import java.io.IOException

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class ZeusApplication

fun main(args: Array<String>) {
    val myObject = object {}
    val serviceAccount = myObject.javaClass.getResourceAsStream("/firebase-sa.json")
            ?: throw IOException("Firebase service account key file not found")

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
        .setDatabaseUrl("https://default.firebaseio.com")
        .build()

    FirebaseApp.initializeApp(options)
    print("Firebase initialized ${serviceAccount}")
    runApplication<ZeusApplication>(*args)
}

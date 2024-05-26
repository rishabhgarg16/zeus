package com.hit11.zeus.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.io.IOException
import javax.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    @Configuration
    class FirebaseConfig {

        @PostConstruct
        fun initialize() {
            if (FirebaseApp.getApps().isEmpty()) {
                try {
                    val serviceAccount =
                        this::class.java.getResourceAsStream("/firebase-sa.json")
                            ?: throw IOException("Firebase service account key file not found")

                    val options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://default.firebaseio.com")
                        .build()

                    FirebaseApp.initializeApp(options)
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw RuntimeException("Failed to initialize Firebase", e)
                }
            }
        }

        @Bean
        fun firestore(): Firestore {
            return FirestoreClient.getFirestore()
        }
    }
}
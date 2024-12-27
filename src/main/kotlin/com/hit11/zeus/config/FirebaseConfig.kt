package com.hit11.zeus.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.InputStream

@Configuration
class FirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        val serviceAccount: InputStream = this::class.java.getResourceAsStream("/firebase-sa.json")
            ?: throw IllegalStateException("Firebase service account file not found")
        
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl("https://default.firebaseio.com")
            .build()

        return if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        } else {
            FirebaseApp.getInstance()
        }
    }

    @Bean
    fun firestore(firebaseApp: FirebaseApp): Firestore {
        val firestore = FirestoreClient.getFirestore(firebaseApp)
        println("Firestore initialized: ${firestore.options.projectId}")
        return firestore
    }

    @Bean
    fun firebaseMessaging(firebaseApp: FirebaseApp): FirebaseMessaging {
        val messaging = FirebaseMessaging.getInstance(firebaseApp)
        println("FirebaseMessaging initialized for app: ${firebaseApp.name}")
        return messaging
    }
}
package com.hit11.zeus.repository;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient

public class FirestoreRepository {

    var projectId = "obsidian"

    lateinit var db: Firestore
    init {
        // Use the application default credentials
        var credentials = GoogleCredentials.getApplicationDefault();
        var options = FirebaseOptions.Builder()
            .setCredentials(credentials)
            .setProjectId(projectId)
            .build();
        FirebaseApp.initializeApp(options);

        db = FirestoreClient.getFirestore();
    }
}

package com.hit11.zeus.service

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Service

@Service
class FirestoreService {

    private val firestore: Firestore = FirestoreClient.getFirestore()

    fun getCollection(collectionName: String) = firestore.collection(collectionName).get()

    fun addDocument(collectionName: String, documentData: Map<String, Any>): String {
        val docRef = firestore.collection(collectionName).add(documentData).get()
        return docRef.id
    }

    fun getDocument(collectionName: String, documentId: String) = firestore.collection(collectionName).document(documentId).get()

    fun updateDocument(collectionName: String, documentId: String, updates: Map<String, Any>) {
        firestore.collection(collectionName).document(documentId).update(updates)
    }

    fun deleteDocument(collectionName: String, documentId: String) {
        firestore.collection(collectionName).document(documentId).delete()
    }
}
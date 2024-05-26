package com.hit11.zeus.controller

import com.hit11.zeus.service.FirestoreService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/firestore")
class FirestoreController(private val firestoreService: FirestoreService) {

    @GetMapping("/{collectionName}")
    fun getCollection(@PathVariable collectionName: String): ResponseEntity<Any> {
        val documents = firestoreService.getCollection(collectionName)
        return ResponseEntity.ok(documents.get().documents.map { it.data })
    }

    @PostMapping("/{collectionName}")
    fun addDocument(@PathVariable collectionName: String, @RequestBody documentData: Map<String, Any>): ResponseEntity<String> {
        val documentId = firestoreService.addDocument(collectionName, documentData)
        return ResponseEntity.ok(documentId)
    }

    @GetMapping("/{collectionName}/{documentId}")
    fun getDocument(@PathVariable collectionName: String, @PathVariable documentId: String): ResponseEntity<Any> {
        val document = firestoreService.getDocument(collectionName, documentId)
        return ResponseEntity.ok(document.get().data)
    }

    @PutMapping("/{collectionName}/{documentId}")
    fun updateDocument(@PathVariable collectionName: String, @PathVariable documentId: String, @RequestBody updates: Map<String, Any>): ResponseEntity<Void> {
        firestoreService.updateDocument(collectionName, documentId, updates)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{collectionName}/{documentId}")
    fun deleteDocument(@PathVariable collectionName: String, @PathVariable documentId: String): ResponseEntity<Void> {
        firestoreService.deleteDocument(collectionName, documentId)
        return ResponseEntity.noContent().build()
    }
}
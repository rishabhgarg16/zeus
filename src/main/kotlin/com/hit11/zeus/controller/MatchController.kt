package com.hit11.zeus.controller

import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import com.hit11.zeus.model.Match
import com.hit11.zeus.service.FirestoreService
import com.hit11.zeus.service.MatchService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/matches")
class MatchController(private val matchService: MatchService, private val firestoreService: FirestoreService) {

    var matchCollection = firestoreService.getCollection("test_matches")
    private val firestore: Firestore = FirestoreClient.getFirestore()
//    @GetMapping("/upcoming")
//    fun getUpcomingMatches(): ResponseEntity<Any> {
//
//        return firestore.collection("test_matches").
//    }
}

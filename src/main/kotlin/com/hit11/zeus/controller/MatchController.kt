package com.hit11.zeus.controller

import com.google.cloud.firestore.Firestore
import com.hit11.zeus.livedata.Hit11Scorecard
import com.hit11.zeus.model.Match
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.model.response.GetMatchApiResponse
import com.hit11.zeus.model.response.toGetMatchApiResponse
import com.hit11.zeus.service.MatchService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/match")
class MatchController(
    private val firestore: Firestore,
    private val matchService: MatchService
) {
    @GetMapping("/upcoming")
    fun getLiveMatches(@RequestParam("limit", defaultValue = "4") limit: Int):
            ResponseEntity<ApiResponse<List<GetMatchApiResponse>>> {
        val data = matchService.getRelevantMatches(limit).map { it.toGetMatchApiResponse() }
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = data
            )
        )
    }

    @GetMapping("/{matchId}")
    fun getMatchById(@PathVariable matchId: Int): ResponseEntity<ApiResponse<Match>> {
        val data = matchService.getMatchById(matchId)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = data
            )
        )
    }

    @GetMapping("/livescore/{matchId}")
    fun getLiveScore(
        @PathVariable matchId: Int,
        @RequestParam(name = "cache", required = false, defaultValue = "true") useCache: Boolean
    ): ResponseEntity<ApiResponse<Hit11Scorecard>> {
        val data = matchService.getScoreByMatch(matchId, useCache)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = data
            )
        )
    }

    @GetMapping("/livescore/cricbuzz/{cricbuzzMatchId}")
    fun getOrCreateLiveScoreByCricbuzz(
        @PathVariable cricbuzzMatchId: Int,
        @RequestParam(name = "cache", required = false, defaultValue = "true") useCache: Boolean
    ): ResponseEntity<ApiResponse<Hit11Scorecard>> {
        val data = matchService.getScoreByCricbuzzMatch(cricbuzzMatchId, useCache)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = data
            )
        )
    }

    @GetMapping("/upload/fixture")
    fun uploadData(): ResponseEntity<ApiResponse<String>> {
        val data = readCsv("/Users/anmolsingh/github/citus/fixtures.csv")
        uploadToFirestore("fixtures_2", data)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = "Data Uploaded"
            )
        )

    }

    private fun readCsv(filePath: String): List<Map<String, String>> {
        val file = File(filePath)
        val rows = mutableListOf<Map<String, String>>()
        val lines = file.readLines()

        if (lines.isEmpty()) {
            throw IllegalArgumentException("CSV file is empty")
        }

        val headers = lines.first().split(",").map { it.trim() }

        for (line in lines.drop(1)) {
            val values = line.split(",").map { it.trim() }
            val row = headers.zip(values).toMap()
            rows.add(row)
        }

        return rows
    }

    private fun uploadToFirestore(collectionName: String, data: List<Map<String, String>>) {
        // Retrieve the current count of documents to generate a sequential human-readable ID
        val collectionRef = firestore.collection(collectionName)
        val currentCount = collectionRef.get().get().size()
        var nextId = currentCount + 1
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        data.forEach { document ->
            try {
                val mappedDocument = mapOf(
                    "id" to nextId, // Human-readable integer ID
                    "firebase_id" to document["firebase_id"],  // This will be updated later
                    "match_number" to document["Match Number"],
                    "match_group" to document["Match Group"],
                    "team_1" to document["Team A"],
                    "team_1_image_url" to document["Team A Image URL"],
                    "team_2" to document["Team B"],
                    "team_2_image_url" to document["Team B Image URL"],
                    "time_gmt" to document["Time (GMT)"],
                    "city" to document["City"],
                    "stadium" to document["Stadium"],
                    "country" to document["Country"],
                    "tournament_name" to document["Tournament Name"],
                    "match_type" to document["Tournament Type"],
                    "match_status" to document["Match Status"],
                    "match_link" to document["Match Link"],
                    "start_date" to ZonedDateTime.parse(document["Start Date"], DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .toEpochSecond(),
                    "uploaded_at" to System.currentTimeMillis(),
                )
                val documentReference = collectionRef.add(mappedDocument).get()
                documentReference.update("firebase_id", documentReference.id).get() // Update with Firebase ID
                println("Document added successfully at: ${documentReference.id} with human-readable ID: $nextId")

                nextId++ // Increment for the next document
            } catch (e: Exception) {
                println("Error adding document: $e")
            }
        }
    }
}
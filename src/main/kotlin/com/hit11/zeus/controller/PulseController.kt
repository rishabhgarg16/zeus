package com.hit11.zeus.controller


import com.hit11.zeus.model.*
import com.hit11.zeus.service.PulseService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/pulse")
class PulseController(private val service: PulseService) {
    private val logger = LoggerFactory.getLogger(PulseController::class.java)

    @GetMapping("/active")
    fun getAllOpinions(@RequestBody matchId: String): List<PulseDataModelResponse>? {
        val response = service.getAllActiveOpinions(matchId)?.map { it.toResponse() }
        return response
    }

    @PostMapping("/user/submit")
    fun submitResponse(@RequestBody request: UserPulseSubmissionRequest): ResponseEntity<UserPulseSubmissionResponse> {
        logger.info("Received request: $request")

        val userPulseDataModel = UserPulseDataModel(
            userId = request.userId,
            pulseId = request.pulseId,
            matchIdRef = request.matchIdRef,
            userAnswer = request.userAnswer,
            answerTime = System.currentTimeMillis() / 1000,
            userWager = request.userWager,
            userResult = request.userResult
        )

        try {
            val savedResponse = service.submitResponse(userPulseDataModel)
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResponse)
        } catch (e: Exception) {
            logger.error("Error while submitting response", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UserPulseSubmissionResponse())
        }
    }

    @GetMapping("/enrolled/{userId}")
    fun getEnrolledPulsesByUser(@PathVariable userId: String): ResponseEntity<List<UserPulseSubmissionResponse>> {
        try {
            val response = service.getEnrolledPulsesByUser(userId)
            return ResponseEntity.status(HttpStatus.OK).body(response)
        } catch (ex: Exception) {
            logger.error("Error while fetching data", ex)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }

    @GetMapping("/enrolled")
    fun getEnrolledPulsesByUserAndMatch(
        @RequestBody request: GetUserEnrolledPulseRequest
    ): ResponseEntity<List<UserPulseSubmissionResponse>> {
        try {
            val response = service.getEnrolledPulsesByUserAndMatch(request.userId, request.matchIdRef)
            return ResponseEntity.status(HttpStatus.OK).body(response)
        } catch (ex: Exception) {
            logger.error("Error while fetching data", ex)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }
}

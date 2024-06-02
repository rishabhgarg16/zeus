package com.hit11.zeus.controller


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.hit11.zeus.model.*
import com.hit11.zeus.service.PulseService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


data class MatchIdRequest(val matchId: String)

@RestController
@RequestMapping("/api/pulse")
class PulseController(private val service: PulseService) {
    private val logger = LoggerFactory.getLogger(PulseController::class.java)

    @PostMapping("/active")
    fun getAllOpinions(@RequestBody request: MatchIdRequest): List<PulseDataModelResponse>? {
        val response = service.getAllActiveOpinions(request.matchId)?.map { it.toResponse() }
        return response
    }

    @PostMapping("/user/submit")
    fun submitResponse(@RequestBody request: UserPulseSubmissionRequest): ResponseEntity<UserPulseSubmissionResponse> {
        logger.info("Received request: $request")

        val userPulseDataModel = UserPulseDataModel(
            userId = request.userId,
            pulseId = request.pulseId,
            matchIdRefString = request.matchIdRef,
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

    @PostMapping("/enrolledabc/matchabc")
    fun getEnrolledPulsesByUserAndMatch(
        @RequestBody req: UserMatchIdRequest,
    ): ResponseEntity<List<UserPulseSubmissionResponse>> {
        try {
            val response = service.getEnrolledPulsesByUserAndMatch(req.userId, req.matchIdRef)
            return ResponseEntity.status(HttpStatus.OK).body(response)
        } catch (ex: Exception) {
            logger.error("Error while fetching data", ex)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }

    @PostMapping("/updateAnswer")
    fun updateAnswer(
        @RequestBody req: PulseAnswerUpdateRequest,
    ): ResponseEntity<PulseAnswerUpdateResponse> {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(service.updatePulseAnswer(req))
        } catch (ex: Exception) {
            logger.error("Error while fetching data", ex)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(PulseAnswerUpdateResponse())
        }
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
data class UserMatchIdRequest(
    val userId: String = "",
    val matchIdRef: String = "",
)

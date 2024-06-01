package com.hit11.zeus.controller


import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.PulseDataModelResponse
import com.hit11.zeus.model.UserPulseDataModel
import com.hit11.zeus.model.UserPulseSubmissionRequest
import com.hit11.zeus.model.toResponse
import com.hit11.zeus.service.OpinionService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/pulse")
class PulseController(private val service: OpinionService) {
    private val logger = LoggerFactory.getLogger(PulseController::class.java)

    @GetMapping("/active/{matchId}")
    fun getAllOpinions(@PathVariable("matchId") matchId: String): List<PulseDataModelResponse>? {
        val response = service.getAllActiveOpinions(matchId)?.map { it.toResponse() }
        return response
    }

    @PostMapping("/user/submit")
    fun submitResponse(@RequestBody request: UserPulseSubmissionRequest): ResponseEntity<UserPulseDataModel> {
        logger.info("Received request: $request")

        val userPulseDataModel = UserPulseDataModel(
            userId = request.userId,
            pulseId = request.pulseIdRef,
            matchId = request.matchIdRef,
            answerChosen = request.userAnswer,
            answerTime = System.currentTimeMillis()/1000,
            userWager = request.userWager
        )
        val savedResponse = service.submitResponse(userPulseDataModel)
        if (savedResponse is UserPulseDataModel) {
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResponse)
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(savedResponse)
        }
    }

    @GetMapping("/{userId}")
    fun getEnrolledPulsesByUser(@PathVariable userId: Int): ResponseEntity<List<UserPulseDataModel>> {
        val response = service.getEnrolledPulsesByUser(userId)
        return if (response != null) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{userId}/{matchId}")
    fun getEnrolledPulsesByUserAndMatch(
        @PathVariable userId: Int,
        @PathVariable matchId: Int
    ): ResponseEntity<List<UserPulseDataModel>> {
        val response = service.getEnrolledPulsesByUserAndMatch(userId, matchId)
        return if (response != null) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

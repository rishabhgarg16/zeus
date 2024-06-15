package com.hit11.zeus.controller


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.hit11.zeus.adapter.UserPulseAdapter
import com.hit11.zeus.adapter.toResponse
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.*
import com.hit11.zeus.service.PulseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank

data class MatchIdRequest(
    @field:NotBlank(message = "User ID cannot be blank")
    val matchId: String = ""
)

@RestController
@RequestMapping("/api/pulse")
class PulseController(private val service: PulseService) {
    private val logger = Logger.getLogger(this::class.java)

    @PostMapping("/active")
    fun getAllOpinions(@Valid @RequestBody request: MatchIdRequest): ResponseEntity<ApiResponse<List<PulseDataModelResponse>?>> {
        val response = service.getAllActiveOpinions(request.matchId)?.map { it.toResponse() }
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = response
            )
        )
    }

    @PostMapping("/user/submit")
    fun submitResponse(@RequestBody request: UserPulseSubmissionRequest): ResponseEntity<ApiResponse<UserPulseSubmissionResponse>> {
        logger.info("Received request: $request")
        val dataModel = UserPulseAdapter.toDataModel(request)
        val savedResponse = service.submitResponse(dataModel)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                status = HttpStatus.CREATED.value(),
                internalCode = null,
                message = "Response submitted successfully",
                data = savedResponse
            )
        )
    }

    @PostMapping("/user/bookOrder")
    fun submitUserTrade(@RequestBody request: UserTradeSubmissionRequest): ResponseEntity<ApiResponse<Boolean>> {
        logger.info("Received request: $request")
        val savedResponse = service.submitUserTrade(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse(
                status = HttpStatus.CREATED.value(),
                internalCode = null,
                message = "Order booked successfully",
                data = savedResponse
            )
        )
    }

    @GetMapping("/enrolled/{userId}")
    fun getEnrolledPulsesByUser(@PathVariable userId: String): ResponseEntity<ApiResponse<List<UserPulseSubmissionResponse>>> {
        val response = service.getEnrolledPulsesByUser(userId)
        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = response
            )
        )
    }

    @PostMapping("/enrolledabc/matchabc")
    fun getEnrolledPulsesByUserAndMatch(@RequestBody req: UserMatchIdRequest): ResponseEntity<ApiResponse<List<UserPulseSubmissionResponse>>> {
        val response = service.getEnrolledPulsesByUserAndMatch(req.userId, req.matchIdRef)
        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = response
            )
        )
    }

    @PostMapping("/updateAnswer")
    fun updateAnswer(@RequestBody req: PulseAnswerUpdateRequest): ResponseEntity<ApiResponse<PulseAnswerUpdateResponse>> {
        val response = service.updatePulseAnswer(req)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Answer updated successfully",
                data = response
            )
        )
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserMatchIdRequest(
    val userId: String = "",
    val matchIdRef: String = "",
)

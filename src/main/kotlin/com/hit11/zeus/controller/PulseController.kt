package com.hit11.zeus.controller

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.QuestionDataModel
import com.hit11.zeus.model.request.GetActivePulseRequest
import com.hit11.zeus.model.request.QuestionAnswerUpdateRequest
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.model.response.QuestionAnswerUpdateResponse
import com.hit11.zeus.service.QuestionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/pulse/")
class QuestionController(private val service: QuestionService) {
    private val logger = Logger.getLogger(this::class.java)

    @GetMapping("/all-active")
    fun getAllActivePulses(): ResponseEntity<ApiResponse<List<QuestionDataModel>?>> {
        try {
            val response = service.getAllActivePulses()
            return ResponseEntity.ok(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    internalCode = null,
                    message = "Successfully fetched active pulses",
                    data = response
                )
            )
        } catch (e: Exception) {
            logger.error("Error fetching active pulses", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    internalCode = null,
                    message = "Error fetching active pulses",
                    data = null
                )
            )
        }
    }

    @PostMapping("/active")
    fun getAllOpinions(
        @Valid @RequestBody request: GetActivePulseRequest
    ): ResponseEntity<ApiResponse<List<QuestionDataModel>?>> {
        val response = service.getAllActiveQuestionsByMatch(request.matchIdList)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = response
            )
        )
    }

    @GetMapping("/{pulseId}")
    fun getPulseById(
        @PathVariable pulseId: Int
    ): ResponseEntity<ApiResponse<QuestionDataModel?>> {
        val response = service.getQuestionById(pulseId)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = response
            )
        )
    }

    @PostMapping("/updateAnswer")
    fun updateAnswer(
        @RequestBody req: QuestionAnswerUpdateRequest
    ): ResponseEntity<ApiResponse<QuestionAnswerUpdateResponse>> {
        val response = service.updateQuestionAnswer(req)
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

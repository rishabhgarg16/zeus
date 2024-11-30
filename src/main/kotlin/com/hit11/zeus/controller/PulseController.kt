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

    @PostMapping("/active")
    fun getAllOpinions(
        @Valid @RequestBody request: GetActivePulseRequest
    ): ResponseEntity<ApiResponse<List<QuestionDataModel>?>> {
        val response = service.getAllActiveQuestions(request.matchIdList)
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

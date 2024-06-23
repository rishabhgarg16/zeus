package com.hit11.zeus.controller


import com.hit11.zeus.adapter.toTradeResponse
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.*
import com.hit11.zeus.service.PulseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class PulseController(private val service: PulseService) {
    private val logger = Logger.getLogger(this::class.java)

    @PostMapping("/pulse/active") fun getAllOpinions(
        @Valid @RequestBody request: GetActivePulseRequest
    ): ResponseEntity<ApiResponse<List<PulseDataModelResponse>?>> {
        val response = service.getAllActiveOpinions(request.matchIdList)?.map { it.toTradeResponse() }
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(), internalCode = null, message = "Success", data = response
            )
        )
    }

    @PostMapping("/updateAnswer")
    fun updateAnswer(
        @RequestBody req: PulseAnswerUpdateRequest
    ): ResponseEntity<ApiResponse<PulseAnswerUpdateResponse>> {
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

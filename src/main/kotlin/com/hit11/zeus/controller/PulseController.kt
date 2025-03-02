package com.hit11.zeus.controller

import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.LastTradedPriceQuestionDTO
import com.hit11.zeus.model.Question
import com.hit11.zeus.model.request.GetActivePulseRequest
import com.hit11.zeus.model.request.LastTradedPriceRequest
import com.hit11.zeus.model.request.QuestionAnswerUpdateRequest
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.model.response.QuestionAnswerUpdateResponse
import com.hit11.zeus.service.QuestionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/pulse")
class QuestionController(
    private val questionService: QuestionService
) {
    private val logger = Logger.getLogger(this::class.java)

    @GetMapping("/all-active")
    fun getAllActivePulses(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "500") size: Int
    ): ResponseEntity<ApiResponse<List<Question>?>> {
        return try {
            val response = questionService.getAllActivePulses(page, size)
            ResponseEntity.ok(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    internalCode = null,
                    message = "Successfully fetched active pulses",
                    data = response
                )
            )
        } catch (e: Exception) {
            logger.error("Error fetching active pulses", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    internalCode = null,
                    message = "Error fetching active pulses",
                    data = null
                )
            )
        }
    }

    @PostMapping("/activate")
    fun activateQuestionsForLiveMatches(): ResponseEntity<ApiResponse<ActivationResponse>> {
        return try {
            val activatedCount = questionService.activateQuestionsForLiveMatches()
            ResponseEntity.ok(
                ApiResponse(
                    status = HttpStatus.OK.value(),
                    message = "Successfully activated questions for live matches",
                    data = ActivationResponse(activatedCount),
                    internalCode = null,
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to activate questions for live matches", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "Failed to activate questions: ${e.message}",
                    data = null,
                    internalCode = null,
                )
            )
        }
    }

    @PostMapping("/active")
    fun getAllOpinions(
        @Valid @RequestBody request: GetActivePulseRequest
    ): ResponseEntity<ApiResponse<List<Question>?>> {
        val response = questionService.getAllActiveQuestionsByMatch(request.matchIdList)
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
    ): ResponseEntity<ApiResponse<Question?>> {
        val response = questionService.getQuestionById(pulseId)
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
        val response = questionService.updateQuestionAnswer(req)
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Answer updated successfully",
                data = response
            )
        )
    }


    @PostMapping("/lastTradedPrice")
    fun lastTradedPrice(
        @RequestBody req: LastTradedPriceRequest
    ): ResponseEntity<ApiResponse<List<LastTradedPriceQuestionDTO>>> {
        val response = questionService.getLastTradedPrice(req.pulseIds)
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

data class ActivationResponse(
    val activatedCount: Int
)

package com.hit11.zeus.controller


import com.hit11.zeus.adapter.addPulseData
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
        val response = service.getAllActiveOpinions(request.matchIdList)?.map { it.addPulseData() }
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(), internalCode = null, message = "Success", data = response
            )
        )
    }

//    @PostMapping("/user/submit")
//    fun submitResponse(
//        @RequestBody request: UserPulseSubmissionRequest
//    ): ResponseEntity<ApiResponse<UserPulseSubmissionResponse>> {
//        logger.info("Received request: $request")
//        val dataModel = UserPulseAdapter.toDataModel(request)
//        val savedResponse = service.submitResponse(dataModel)
//        return ResponseEntity.status(HttpStatus.CREATED).body(
//            ApiResponse(
//                status = HttpStatus.CREATED.value(),
//                internalCode = null,
//                message = "Response submitted successfully",
//                data = savedResponse
//            )
//        )
//    }





//    @PostMapping("/updateAnswer")
//    fun updateAnswer(
//        @RequestBody req: PulseAnswerUpdateRequest
//    ): ResponseEntity<ApiResponse<PulseAnswerUpdateResponse>> {
//        val response = service.updatePulseAnswer(req)
//        return ResponseEntity.ok(
//            ApiResponse(
//                status = HttpStatus.OK.value(),
//                internalCode = null,
//                message = "Answer updated successfully",
//                data = response
//            )
//        )
//    }
}

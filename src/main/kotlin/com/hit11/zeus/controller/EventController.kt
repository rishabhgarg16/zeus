package com.hit11.zeus.controller

import com.hit11.zeus.model.ApiResponse;
import com.hit11.zeus.model.BallEvent;
import com.hit11.zeus.service.EventService
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventController(private val eventService: EventService) {

    @PostMapping("/ball")
    fun receiveBallEvent(@RequestBody ballEvent: BallEvent): ResponseEntity<ApiResponse<String>> {
        eventService.processBallEvent(ballEvent);
        return ResponseEntity.ok(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Event processed successfully",
                data = "Event processed successfully"
            )
        )
    }
}

package com.hit11.zeus.controller

import com.hit11.zeus.model.UserPosition
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.service.UserPositionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/positions")
class UserPositionController(private val userPositionService: UserPositionService) {

    @GetMapping("/user/{userId}/pulse/{pulseId}")
    fun getUserPositionsByPulse(
        @PathVariable userId: Int,
        @PathVariable pulseId: Int
    ): ResponseEntity<List<UserPosition>> {
        val position = userPositionService.getPositionsByUserAndPulse(userId, pulseId)
        return ResponseEntity.ok(position)
    }

    @GetMapping("/user/{userId}")
    fun getPositionsByUser(@PathVariable userId: Int): ResponseEntity<ApiResponse<List<UserPosition>>> {
        val positions = userPositionService.getAllUserPositions(userId)
        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = positions
            )
        )
    }

    @GetMapping("/pulse/{pulseId}")
    fun getPositionsByPulse(@PathVariable pulseId: Int): ResponseEntity<ApiResponse<List<UserPosition>>> {
        val positions = userPositionService.getPositionsByPulse(pulseId)
        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse(
                status = HttpStatus.OK.value(),
                internalCode = null,
                message = "Success",
                data = positions
            )
        )
    }
}

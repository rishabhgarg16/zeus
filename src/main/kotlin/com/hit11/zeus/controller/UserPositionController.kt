package com.hit11.zeus.controller

import com.hit11.zeus.model.UserPosition
import com.hit11.zeus.service.UserPositionService
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/positions")
class UserPositionController(private val userPositionService: UserPositionService) {

    @GetMapping("/user/{userId}/pulse/{pulseId}")
    fun getUserPosition(@PathVariable userId: Int, @PathVariable pulseId: Int): ResponseEntity<UserPosition> {
        val position = userPositionService.getPosition(userId, pulseId)
        return ResponseEntity.ok(position)
    }

    @GetMapping("/user/{userId}")
    fun getAllUserPositions(@PathVariable userId: Int): ResponseEntity<List<UserPosition>> {
        val positions = userPositionService.getAllUserPositions(userId)
        return ResponseEntity.ok(positions)
    }

    @GetMapping("/pulse/{pulseId}")
    fun getPositionsByPulse(@PathVariable pulseId: Int): ResponseEntity<List<UserPosition>> {
        val positions = userPositionService.getPositionsByPulse(pulseId)
        return ResponseEntity.ok(positions)
    }

    @GetMapping("/pulse/{pulseId}/user/{userId}")
    fun getUserPositionInPulse(
        @PathVariable pulseId: Int,
        @PathVariable userId: Int
    ): ResponseEntity<UserPosition> {
        val position = userPositionService.getPosition(userId, pulseId)
        return ResponseEntity.ok(position)
    }
}
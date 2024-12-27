package com.hit11.zeus.controller

import com.hit11.zeus.notification.NotificationPayload
import com.hit11.zeus.notification.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @PostMapping("/send")
    fun sendNotification(@RequestBody @Valid payload: NotificationPayload): ResponseEntity<String> {
        return try {
            notificationService.handleNotification(payload)
            ResponseEntity.ok("Notification sent successfully")
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("Failed to send notification: ${e.message}")
        }
    }
}

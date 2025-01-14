package com.hit11.zeus.controller

import com.google.firebase.auth.UserRecord
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.model.User
import com.hit11.zeus.model.UserReward
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    private val logger = Logger.getLogger(this::class.java)

    @GetMapping("/{firebaseUID}")
    fun getUserDetails(@PathVariable("firebaseUID") firebaseUID: String): UserRecord? {
        return userService.getUserFromAuth(firebaseUID)
    }

    @GetMapping("/internal/{firebaseUID}")
    fun getInternalUserByFirebaseID(
        @PathVariable("firebaseUID") @NotBlank firebaseUID: String
    ): ResponseEntity<ApiResponse<User>> {
        return try {
            val user = userService.getUser(firebaseUID)
            user?.let {
                ResponseEntity.ok(
                    ApiResponse(
                        data = it,
                        status = HttpStatus.OK.value(),
                        message = "User details retrieved successfully",
                        internalCode = null
                    )
                )
            } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse(
                    data = null,
                    status = HttpStatus.NOT_FOUND.value(),
                    message = "Internal user not found",
                    internalCode = "USER_NOT_FOUND"
                )
            )
        } catch (e: Exception) {
            logger.error("Error retrieving internal user", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    data = null,
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "Error retrieving internal user",
                    internalCode = "INTERNAL_SERVER_ERROR"
                )
            )
        }
    }

    @PostMapping("/reward/{firebaseUID}")
    fun checkUserReward(@PathVariable("firebaseUID") firebaseUID: String): UserReward? {
        return userService.checkUserReward(firebaseUID)
    }

    @PostMapping("/createnew/{firebaseUID}")
    fun createNew(
        @PathVariable("firebaseUID") @NotBlank firebaseUID: String,
        @RequestParam("fcmToken") @NotBlank fcmToken: String
    ): ResponseEntity<ApiResponse<User>> {
        return try {
            val user = userService.createUser(firebaseUID, fcmToken)
            ResponseEntity.ok(
                ApiResponse(
                    data = user,
                    status = HttpStatus.CREATED.value(),
                    message = "User created successfully",
                    internalCode = null
                )
            )
        } catch (e: Exception) {
            logger.error("Error creating internal user", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    message = "Error creating user",
                    data = null,
                    internalCode = null,
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value()
                )
            )
        }
    }

    @PostMapping("/fcm/{firebaseUID}")
    fun updateFCMToken(
        @PathVariable("firebaseUID") @NotBlank firebaseUID: String,
        @RequestParam("token") @NotBlank fcmToken: String
    ): ResponseEntity<ApiResponse<Boolean>> {
        return try {
            val updated = userService.updateFCMToken(firebaseUID, fcmToken)
            ResponseEntity.ok(
                ApiResponse(
                    data = updated,
                    status = HttpStatus.OK.value(),
                    message = "FCM token updated successfully",
                    internalCode = null
                )
            )
        } catch (e: Exception) {
            logger.error("Error updating FCM token", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    data = false,
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "Error updating FCM token",
                    internalCode = "INTERNAL_SERVER_ERROR"
                )
            )
        }
    }
}
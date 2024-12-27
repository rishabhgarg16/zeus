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
    fun getInternalUser(@PathVariable("firebaseUID") firebaseUID: String): User? {
        return userService.getUser(firebaseUID)
    }

    @PostMapping("/reward/{firebaseUID}")
    fun checkUserReward(@PathVariable("firebaseUID") firebaseUID: String): UserReward? {
        return userService.checkUserReward(firebaseUID)
    }

    @GetMapping("/createnew")
    fun createNew(
        @RequestParam("firebaseUID") firebaseUID: String,
        @RequestParam("fcmToken") fcmToken: String
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
}
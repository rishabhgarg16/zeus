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
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.hit11.zeus.service.sms.Fast2SmsService
import java.time.Instant
import java.util.Date

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val fast2SmsService: Fast2SmsService
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


    @PostMapping("/login")
    fun loginHit11(
        @RequestBody otpRequest: OtpRequest
    ): ResponseEntity<ApiResponse<OtpResponse>> {
        try {
            if (otpRequest.otp == "786589") {
                val user = userService.getOrCreateUser(otpRequest.phoneNumber)
                val otpResponse = createJwt(user)
                return ResponseEntity.ok(
                    ApiResponse(
                        data = otpResponse,
                        status = HttpStatus.OK.value(),
                        message = "FCM token updated successfully",
                        internalCode = null
                    )
                )
            }
            throw OtpMismatchException()
        } catch (e: Exception) {
            logger.error("Error validating otp", e)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse(
                    data = null,
                    status = HttpStatus.UNAUTHORIZED.value(),
                    message = "Error validating otp",
                    internalCode = "INTERNAL_SERVER_ERROR"
                )
            )
        }
    }

    @PostMapping("/generateotp")
    fun generateOtp(
        @RequestBody otpRequest: GenerateOtpRequest
    ): ResponseEntity<ApiResponse<Boolean>> {
        try {
            fast2SmsService.sendOtp(otpRequest.phoneNumber)
            return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse(
                    data = true,
                    status = HttpStatus.OK.value(),
                    message = "Otp generated successfully",
                    internalCode = "OTP_GENERATED"
                )
            )
        } catch (e: Exception) {
            logger.error("Error generating otp", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse(
                    data = null,
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "Error generating otp",
                    internalCode = "INTERNAL_SERVER_ERROR"
                )
            )
        }
    }
}


data class OtpRequest @JsonCreator constructor(
    @JsonProperty("phoneNumber") val phoneNumber: String,
    @JsonProperty("otp") val otp: String
)

data class GenerateOtpRequest @JsonCreator constructor(
    @JsonProperty("phoneNumber") val phoneNumber: String
)


data class OtpResponse(
    val phoneNumber: String = "",
    val jwt: String = ""
)

class OtpMismatchException(message: String = "The provided OTP is incorrect.") : Exception(message)

fun createJwt(
    user: User
): OtpResponse {
    val algorithm = Algorithm.HMAC256("secret") // Choose your preferred algorithm
    val expiry = Instant.now().plusSeconds(15 * 60) // Calculate expiration time
    val token = JWT.create()
        .withIssuer("hit11") // Set your issuer (e.g., your app name)
        .withClaim("phoneNumber", user.phone)
        .withClaim("name", user.name)
        .withClaim("email", user.email)
        .withExpiresAt(Date.from(expiry)) // Set the expiration time
        .sign(algorithm) // Sign the token
    return OtpResponse(
        phoneNumber = user.phone,
        jwt = token
    )
}
package com.hit11.zeus.controller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.firebase.auth.UserRecord
import com.hit11.zeus.config.UserClaimsContext
import com.hit11.zeus.exception.Logger
import com.hit11.zeus.exception.UserNotFoundException
import com.hit11.zeus.model.User
import com.hit11.zeus.model.UserReward
import com.hit11.zeus.model.WalletTransaction
import com.hit11.zeus.model.WalletTransactionRow
import com.hit11.zeus.model.response.ApiResponse
import com.hit11.zeus.service.UserService
import com.hit11.zeus.service.WalletService
import com.hit11.zeus.service.sms.Fast2SmsService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.*
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val fast2SmsService: Fast2SmsService,
    private val walletService: WalletService
) {
    private val logger = Logger.getLogger(this::class.java)

    @GetMapping("/{firebaseUID}")
    fun getUserDetails(@PathVariable("firebaseUID") firebaseUID: String): UserRecord? {
        return userService.getUserFromAuth(firebaseUID)
    }

    @GetMapping("/internal/{userId}")
    fun getInternalUserByFirebaseID(
        @PathVariable("userId") @NotBlank userId: Int
    ): ResponseEntity<ApiResponse<User>> {
        return try {
            val user = userService.getUser(userId)
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
                    message = e.message ?: "Error retrieving internal user",
                    internalCode = "INTERNAL_SERVER_ERROR"
                )
            )
        }
    }

//    @PostMapping("/reward/{firebaseUID}")
//    fun checkUserReward(@PathVariable("firebaseUID") firebaseUID: String): UserReward? {
//        return userService.checkUserReward(firebaseUID)
//    }

    @PostMapping("/reward/{userId}")
    fun checkUserRewardForHit11User(@PathVariable("userId") userId: Int): UserReward? {
        return userService.checkUserReward(userId)
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
                    message = e.message ?: "Error creating user",
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
                    message = e.message ?: "Error updating FCM token",
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
            if (otpRequest.otp == "786598") {
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
                    message = e.message ?: "Error validating otp",
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
                    data = false,
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = e.message ?: "Error generating otp",
                    internalCode = "INTERNAL_SERVER_ERROR"
                )
            )
        }
    }


    @GetMapping("/transactions")
    fun getTransactions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<ApiResponse<Page<WalletTransactionRow>>> {
        val pageable: Pageable = PageRequest.of(page, size)
        val userClaims = UserClaimsContext.getUserClaims() ?: throw UserNotFoundException("User not Found")

        val transactions  = walletService.getTransactions(userClaims.id, pageable)
        return ResponseEntity.status(HttpStatus.OK).body(
            ApiResponse(
                data = transactions,
                status = HttpStatus.OK.value(),
                message = "Otp generated successfully",
                internalCode = "OTP_GENERATED"
            )
        )
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

data class TokenUserClaims(
    val id: Int,
    val email: String,
    val name: String,
    val phone: String
)

private fun createJwt(
    user: User
): OtpResponse {
    val algorithm = Algorithm.HMAC256("secret") // Choose your preferred algorithm
    val expiry = Instant.now().plusSeconds(7 * 24 * 60 * 60) // Calculate expiration time
    val token = JWT.create()
        .withIssuer("hit11") // Set your issuer (e.g., your app name)
        .withClaim("id", user.id)
        .withClaim("email", user.email)
        .withClaim("name", user.name)
        .withClaim("phone", user.phone)
//        .withClaim("deposited_balance", user.depositedBalance.toDouble())
//        .withClaim("winnings_balance", user.winningsBalance.toDouble())
//        .withClaim("promotional_balance", user.promotionalBalance.toDouble())
//        .withClaim("reserved_balance", user.reservedBalance.toDouble())
        .withExpiresAt(Date.from(expiry)) // Set the expiration time
        .sign(algorithm) // Sign the token
    return OtpResponse(
        phoneNumber = user.phone,
        jwt = token
    )
}
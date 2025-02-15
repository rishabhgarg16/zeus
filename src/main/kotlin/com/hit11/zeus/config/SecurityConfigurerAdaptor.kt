package com.hit11.zeus.config

import antlr.Token
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.hit11.zeus.controller.TokenUserClaims
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


object UserClaimsContext {
    private val userClaims = ThreadLocal<TokenUserClaims>()

    fun setUserClaims(claims: TokenUserClaims) {
        userClaims.set(claims)
    }

    fun getUserClaims(): TokenUserClaims? {
        return userClaims.get()
    }

    fun clear() {
        userClaims.remove()
    }
}
@Component
class AuthInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        println("Here")
        try {
            if (request.requestURI == "/api/users/login") {
                return true
            }
            val token = request.getHeader("Authorization")
            val userClaims = verifyToken(token, "secret")
            UserClaimsContext.setUserClaims(userClaims)
            return true // Return true to continue the request
        } catch (e: Exception) {
//            response.status = HttpStatus.UNAUTHORIZED.value()
            return true
        }
    }
    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        UserClaimsContext.clear()
    }
}

private fun verifyToken(token: String, secret: String): TokenUserClaims {
    var finalToken = token
    if (token.startsWith("Bearer ")) {
        finalToken = token.substring(7)
    }
    val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer("hit11") // Same as when you created the token
        .build() // Reusable verifier instance
    val decodedJWT = verifier.verify(finalToken)
    val tokenClaims = TokenUserClaims(
        id = decodedJWT.getClaim("id")?.asInt() ?: throw JWTDecodeException("ID claim is missing"),
        email = decodedJWT.getClaim("email")?.asString() ?: "",
        name = decodedJWT.getClaim("name")?.asString() ?: "",
        phone = decodedJWT.getClaim("phone")?.asString() ?: ""
    )
    return  try {
        verifier.verify(finalToken)
        tokenClaims
    } catch (e: JWTVerificationException) {
        throw e
    }
}


@Configuration
class WebConfig @Autowired constructor(private val authInterceptor: AuthInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
    }
}

package com.hit11.zeus.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        println("Here")
        try {
            if (request.requestURI == "/api/users/login") {
                return true
            }
            val token = request.getHeader("Authorization")
            verifyToken(token, "secret")
            return true // Return true to continue the request
        } catch (e: Exception) {
//            response.status = HttpStatus.UNAUTHORIZED.value()
            return true
        }
    }
}

private fun verifyToken(token: String, secret: String): com.auth0.jwt.interfaces.DecodedJWT {
    val algorithm = Algorithm.HMAC256(secret) // Same as when you created the token
    val verifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer("hit11") // Same as when you created the token
        .build() // Reusable verifier instance
    return verifier.verify(token)
}


@Configuration
class WebConfig @Autowired constructor(private val authInterceptor: AuthInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
    }
}

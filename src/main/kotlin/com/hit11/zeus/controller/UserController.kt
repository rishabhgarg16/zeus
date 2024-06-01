package com.hit11.zeus.controller

import com.google.firebase.auth.UserRecord
import com.hit11.zeus.model.User
import com.hit11.zeus.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/{firebaseUID}")
    fun getUserDetails(@PathVariable("firebaseUID") firebaseUID: String): UserRecord? {
        return userService.getUserFromAuth(firebaseUID)
    }

    @GetMapping("/internal/{firebaseUID}")
    fun getInternalUser(@PathVariable("firebaseUID") firebaseUID: String): User? {
        return userService.getUser(firebaseUID)
    }
}
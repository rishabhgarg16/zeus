package com.hit11.zeus.controller

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
    fun getUserDetails(@PathVariable("firebaseUID") firebaseUID: String): Any? {
        val userDetails = userService.getUserFromAuth(firebaseUID)
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server error");
        } else {
            val documentSnapshot = userService.getUser(userDetails.uid).get()
            if (!documentSnapshot.exists()) {
                val createdUser = userService.createUser(
                    User(0,userDetails.uid, userDetails.email, userDetails.displayName, userDetails.phoneNumber, 500.0,0.0)
                )
                if (createdUser == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server error. Can't create user");
                }
                return createdUser
            } else {
                val user = documentSnapshot.toObject(User::class.java)
                return  user
            }
        }
    }
}
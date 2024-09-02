package com.hit11.zeus.repository

import com.hit11.zeus.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface UserRepository : JpaRepository<User, Int> {
    fun findByFirebaseUID(firebaseUID: String): User?
    fun save(user: User): User
}
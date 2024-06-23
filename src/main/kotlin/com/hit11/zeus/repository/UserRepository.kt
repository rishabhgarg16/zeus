package com.hit11.zeus.repository

import com.hit11.zeus.model.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface UserRepository : JpaRepository<UserEntity, Int> {
    fun findByFirebaseUID(firebaseUID: String): UserEntity?
    fun save(user: UserEntity): UserEntity
}
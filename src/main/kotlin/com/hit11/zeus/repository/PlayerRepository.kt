package com.hit11.zeus.repository

import com.hit11.zeus.model.Player
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlayerRepository : JpaRepository<Player, Int> {
    fun findAllByCountry(country: String): List<Player>
}

package com.hit11.zeus.service

import com.hit11.zeus.model.Player
import com.hit11.zeus.repository.PlayerRepository
import org.springframework.stereotype.Service

@Service
class PlayerService(private val playerRepository: PlayerRepository) {

    fun getPlayerListByCountry(country: String): List<Player> {
        return playerRepository.findAllByCountry(country) ?: emptyList()
    }
}

package com.hit11.zeus.livedata

import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class DataService {

    private val restTemplate = RestTemplate()

    fun fetchMatchData(matchId: Int): String {
        val url = "https://demo.entitysport.com/wp-admin/admin-ajax.php?action=wpec_api_request&path=matches%2F${matchId}%2Finfo"
        return restTemplate.getForObject(url, String::class.java) ?: throw RuntimeException("Failed to fetch data")
    }
}

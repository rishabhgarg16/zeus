package com.hit11.zeus.config

import OpinionDataModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.hit11.zeus.model.Match
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        val module = SimpleModule()
        module.addDeserializer(Match::class.java, MatchDeserializer())

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(module)

        return objectMapper
    }
}
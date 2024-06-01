package com.hit11.zeus.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.module.SimpleModule
import com.hit11.zeus.model.Match
import com.hit11.zeus.model.Option
import com.hit11.zeus.model.PulseDataModel
import com.hit11.zeus.model.UserPulseDataModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        val module = SimpleModule()
        module.addDeserializer(Match::class.java, MatchDeserializer())
        module.addDeserializer(PulseDataModel::class.java, PulseDataModelDeserializer())
        module.addDeserializer(Option::class.java, OptionDeserializer())
        module.addDeserializer(UserPulseDataModel::class.java, UserPulseDeserializer())

        val objectMapper = ObjectMapper()
        objectMapper.registerModule(module)
        objectMapper.apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }

        return objectMapper
    }
}
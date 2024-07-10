package com.hit11.zeus.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration @EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic")  // Enables a simple in-memory message broker to carry the messages back to the client on destinations prefixed with "/topic".
        config.setApplicationDestinationPrefixes("/app")  // Defines the prefix for messages that are bound for methods annotated with @MessageMapping.
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/testWs").setAllowedOrigins("http://localhost:63342").withSockJS()
        registry.addEndpoint("/gs-guide-websocket").setAllowedOrigins("http://localhost:63342").withSockJS()
    }

//    @Bean
//    fun simpMessagingTemplate(brokerChannel: MessageChannel): SimpMessagingTemplate {
//        return SimpMessagingTemplate(brokerChannel)
//    }

}

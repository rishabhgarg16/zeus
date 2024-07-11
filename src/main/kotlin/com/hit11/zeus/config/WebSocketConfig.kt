package com.hit11.zeus.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration @EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic")
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                    "http://localhost:[*]",
                    "http://192.168.*.*:[*]",
                    "http://10.194.243.29:[*]",
                    "http://10.*.*.*:[*]",
                    "capacitor://localhost",
                    "http://localhost"
                )

                .withSockJS()
    }
}

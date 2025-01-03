package com.hit11.zeus.websocket

data class WebSocketSubscriptionMessage(
    val action: String = "",  // "subscribe" or "unsubscribe"
    val type: String = "",    // "MATCH" or "NOTIFICATION"
    val topic: String = "",
    val metadata: Map<String, Any> = emptyMap()  // Additional metadata for the subscription, e.g., matchId or userId
)

enum class SubscriptionType {
    MATCH,
    NOTIFICATION,
}

enum class SubscriptionAction {
    SUBSCRIBE,
    UNSUBSCRIBE
}
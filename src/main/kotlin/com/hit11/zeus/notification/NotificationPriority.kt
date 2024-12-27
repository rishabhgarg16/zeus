package com.hit11.zeus.notification

enum class NotificationPriority {
    HIGH,   // Immediate delivery (trades, orders)
    MEDIUM, // Short delay acceptable (wallet)
    LOW     // Can be batched (matches, general updates)
}
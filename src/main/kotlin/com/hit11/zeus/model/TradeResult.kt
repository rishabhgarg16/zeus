package com.hit11.zeus.model

enum class TradeResult(val text: String, val outcome: Int) {
    WIN("Win", 1),
    LOSE("Lose", 2),
    ACTIVE("Active", 3);

    companion object {
        fun fromText(userResult: String?): TradeResult {
            when (userResult) {
                "Win" -> return WIN
                "Lose" -> return LOSE
                "Active" -> return ACTIVE
            }
            return ACTIVE
        }
    }
}
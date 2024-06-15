package com.hit11.zeus.model

enum class UserResult(val text: String, val outcome: Int) {
    WIN("Win", 1),
    LOSE("Lose", 2),
    ACTIVE("Active", 3), ;

    companion object {
        fun fromText(userResult: String): UserResult {
            when (userResult) {
                "Yes" -> return WIN
                "No" -> return LOSE
                "Active" -> return ACTIVE
            }
            return ACTIVE
        }
    }
}
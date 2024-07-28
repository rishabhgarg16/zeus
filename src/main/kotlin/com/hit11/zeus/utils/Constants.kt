package com.hit11.zeus.utils

import java.math.BigDecimal

interface Constants {
    companion object {
        val BIG_DECIMAL_TEN = BigDecimal("10.00")
        const val MAX_BET_AMOUNT = 1000
        const val MIN_BET_AMOUNT = 1
    }

    interface ErrorMessages {
        companion object {
            const val INVALID_BET_AMOUNT = "Bet amount is invalid"
        }
    }

    interface DatabaseConstants {
        companion object {
            const val MAX_VARCHAR_LENGTH = 255
        }
    }
}
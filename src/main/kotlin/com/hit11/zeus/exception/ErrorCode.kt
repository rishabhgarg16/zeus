package com.hit11.zeus.exception

enum class ErrorCode(val code: String, val message: String) {
    INSUFFICIENT_BALANCE("ERR001", "Insufficient balance for the transaction"),
    ORDER_INVALIDATION("ERR002", "The order is invalid"),
    VALIDATION_ERROR("ERR003", "Validation error"),
    UNEXPECTED_ERROR("ERR000", "An unexpected error occurred")
}
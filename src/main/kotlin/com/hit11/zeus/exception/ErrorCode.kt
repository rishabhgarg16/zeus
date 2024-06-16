package com.hit11.zeus.exception

enum class ErrorCode(val code: String, val message: String) {
    UNEXPECTED_ERROR("ERR000", "An unexpected error occurred"),
    INSUFFICIENT_BALANCE("ERR001", "Insufficient balance for the transaction"),
    ORDER_INVALIDATION("ERR002", "The order is invalid"),
    ORDER_NOT_FOUND("ERR003", "The order is invalid"),
    ORDER_NOT_SAVED("ERR004", "The order is invalid"),
    VALIDATION_ERROR("ERR001000", "Validation error");
}
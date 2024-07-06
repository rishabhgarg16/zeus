package com.hit11.zeus.exception

import com.hit11.zeus.model.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = Logger.getLogger(this::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Any>> {
        val errors = ex.bindingResult.allErrors.map { error ->
            val fieldError = error as FieldError
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }

        logger.error("Validation error: ${errors.joinToString(", ")}")

        val errorResponse = ApiResponse<Any>(
            status = HttpStatus.BAD_REQUEST.value(),
            internalCode = "VALIDATION_ERROR",
            message = "Validation failed",
            data = null,
            errors = errors
        )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InsufficientBalanceException::class)
    fun handleInsufficientBalanceException(ex: InsufficientBalanceException): ResponseEntity<ApiResponse<Any>> {
        logger.error("Insufficient balance: ${ex.message}")
        val errorResponse = ApiResponse<Any>(
            status = HttpStatus.OK.value(),
            internalCode = ErrorCode.INSUFFICIENT_BALANCE.code,
            message = ErrorCode.INSUFFICIENT_BALANCE.message,
            data = null
        )
        return ResponseEntity(errorResponse, HttpStatus.OK)
    }

    @ExceptionHandler(OrderInvalidationException::class)
    fun handleOrderInvalidationException(ex: OrderInvalidationException): ResponseEntity<ApiResponse<Any>> {
        logger.error("Order invalidation: ${ex.message}")
        val errorResponse = ApiResponse<Any>(
            status = HttpStatus.OK.value(),
            internalCode = ErrorCode.ORDER_INVALIDATION.code,
            message = ErrorCode.ORDER_INVALIDATION.message,
            data = null
        )
        return ResponseEntity(errorResponse, HttpStatus.OK)
    }

    @ExceptionHandler(OrderNotFoundException::class)
    fun handleOrderNotFoundException(ex: OrderNotFoundException): ResponseEntity<ApiResponse<Any>> {
        logger.error("Order not found: ${ex.message}")
        val errorResponse = ApiResponse<Any>(
            status = HttpStatus.OK.value(),
            internalCode = ErrorCode.ORDER_NOT_FOUND.code,
            message = ErrorCode.ORDER_NOT_FOUND.message,
            data = null
        )
        return ResponseEntity(errorResponse, HttpStatus.OK)
    }

    @ExceptionHandler(OrderNotSaveException::class)
    fun handleOrderNotSaveException(ex: OrderNotSaveException): ResponseEntity<ApiResponse<Any>> {
        logger.error("Order not saved to DB: ${ex.message}")
        val errorResponse = ApiResponse<Any>(
            status = HttpStatus.OK.value(),
            internalCode = ErrorCode.ORDER_NOT_SAVED.code,
            message = ErrorCode.ORDER_NOT_SAVED.message,
            data = null
        )
        return ResponseEntity(errorResponse, HttpStatus.OK)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<ApiResponse<Any>> {
        logger.error("General error: ${ex.message}", ex)
        val errorResponse = ApiResponse<Any>(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            internalCode = ErrorCode.UNEXPECTED_ERROR.code,
            message = ex.message,
            data = null
        )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
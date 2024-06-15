package com.hit11.zeus.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ApiResponse<T>(
    val status: Int,
    val internalCode: String?,
    val message: String?,
    val data: T?,
    val errors: List<String>? = null,
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
)
package com.hit11.zeus.exception

import kotlin.contracts.contract

class InsufficientBalanceException(message: String) : RuntimeException(message)
class OrderInvalidationException(message: String) : RuntimeException(message)
class OrderNotFoundException (message: String) : RuntimeException(message)
class OrderNotSaveException (message: String) : RuntimeException(message)
class ResourceNotFoundException (message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)
class QuestionValidationException(message: String) : RuntimeException(message)
class OrderValidationException(message: String) : RuntimeException(message)



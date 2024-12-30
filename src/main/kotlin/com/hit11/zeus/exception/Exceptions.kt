package com.hit11.zeus.exception

class InsufficientBalanceException(message: String) : RuntimeException(message)
class OrderInvalidationException(message: String) : RuntimeException(message)
class OrderNotFoundException (message: String) : RuntimeException(message)
class OrderCreationException (message: String) : RuntimeException(message)
class OrderProcessingException (message: String, exception: Exception) : RuntimeException(message, exception)
class ResourceNotFoundException (message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)
class UserAlreadyExistsException(message: String) : RuntimeException(message)
class QuestionValidationException(message: String) : RuntimeException(message)
class OrderValidationException(message: String) : RuntimeException(message)



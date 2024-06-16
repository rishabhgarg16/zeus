package com.hit11.zeus.exception

class InsufficientBalanceException(message: String) : RuntimeException(message)
class OrderInvalidationException(message: String) : RuntimeException(message)
class OrderNotFoundException (message: String) : RuntimeException(message)
class OrderNotSaveException (message: String) : RuntimeException(message)

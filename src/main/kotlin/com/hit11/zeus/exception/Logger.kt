package com.hit11.zeus.exception

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logger {
    fun getLogger(forClass: Class<*>): Logger {
        return LoggerFactory.getLogger(forClass)
    }
}
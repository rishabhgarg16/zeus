package com.hit11.zeus.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

// Example of manual registration if automatic detection does not work
val mapper = jacksonObjectMapper().registerKotlinModule()
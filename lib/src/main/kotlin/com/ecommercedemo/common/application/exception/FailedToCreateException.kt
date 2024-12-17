package com.ecommercedemo.common.application.exception

class FailedToCreateException(message: String, cause: Throwable) : RuntimeException(message, cause)
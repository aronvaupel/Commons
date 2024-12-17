package com.ecommercedemo.common.application.exception

class FailedToHandleEventException(message: String, cause: Throwable) : RuntimeException(message, cause)
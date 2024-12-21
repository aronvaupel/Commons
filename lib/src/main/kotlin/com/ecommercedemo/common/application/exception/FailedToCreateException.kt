package com.ecommercedemo.common.application.exception

//Todo: learn how to annotate with @ResponseStatus and add to all request related exceptions
class FailedToCreateException(message: String, cause: Throwable) : RuntimeException(message, cause)
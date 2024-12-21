package com.ecommercedemo.common.application.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class NullKeyInZSetException(
    message: String,
    private val zSetKey: String,
    private val entry: Any?
) : RuntimeException(message) {
    override fun toString(): String {
        return "NullKeyInZSetException(message=${message}, zSetKey=$zSetKey, entry=$entry)"
    }
}
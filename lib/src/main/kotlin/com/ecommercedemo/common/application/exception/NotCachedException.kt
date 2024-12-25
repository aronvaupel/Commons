package com.ecommercedemo.common.application.exception

class NotCachedException : RuntimeException("Not cached") {
    override fun toString(): String {
        return "NotCachedException(message=${message})"
    }
}
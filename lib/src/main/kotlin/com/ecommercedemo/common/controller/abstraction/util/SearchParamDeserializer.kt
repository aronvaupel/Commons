package com.ecommercedemo.common.controller.abstraction.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class SearchParamDeserializer(private val objectMapper: ObjectMapper) {

    fun convertAnyIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        val result = when (value) {
            is Collection<*> -> {
                value.map { element ->
                    convertIfNeeded(element, expectedType) }
            }
            else -> convertIfNeeded(value, expectedType)
        }
        return result
    }

    private fun convertIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        return value?.takeIf { expectedType.isInstance(it) } ?: objectMapper.convertValue(value, expectedType)
    }

}
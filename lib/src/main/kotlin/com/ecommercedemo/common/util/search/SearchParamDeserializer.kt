package com.ecommercedemo.common.util.search

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class SearchParamDeserializer(private val objectMapper: ObjectMapper) {

    fun convertAnyIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        return when (value) {
            is Collection<*> -> {
                println("Collection detected by deserializer")
                value.map { element ->
                    convertIfNeeded(element, expectedType) }
            }
            else -> convertIfNeeded(value, expectedType)
        }
    }

    private fun convertIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        return value?.takeIf { expectedType.isInstance(it) } ?: objectMapper.convertValue(value, expectedType)
    }

}
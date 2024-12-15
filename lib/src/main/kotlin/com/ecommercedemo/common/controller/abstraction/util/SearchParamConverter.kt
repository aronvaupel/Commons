package com.ecommercedemo.common.controller.abstraction.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class SearchParamConverter(private val objectMapper: ObjectMapper) {

    fun convertAnyIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        println("INPUT: value: $value, expectedType: $expectedType")
        val result = when (value) {
            is Collection<*> -> {
                value.map { element ->
                    convertIfNeeded(element, expectedType) }
            }
            else -> convertIfNeeded(value, expectedType)
        }
        println("CONVERTED VALUE: $result")
        return result
    }

    private fun convertIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        return value?.takeIf { expectedType.isInstance(it) } ?: objectMapper.convertValue(value, expectedType)
    }

}
package com.ecommercedemo.common.controller.abstraction.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class SearchParamDeserializer(private val objectMapper: ObjectMapper) {

    fun convertAnyIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        println("VALUE: $value, EXPECTED TYPE: $expectedType")
        return when (value) {
            is Collection<*> -> {
                println("Collection detected by deserializer")
                value.map { element ->
                    println("ELEMENT: $element")
                    convertIfNeeded(element, expectedType) }
            }
            else -> convertIfNeeded(value, expectedType)
        }
    }

    private fun convertIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        println("Converting value")
        println("VALUE: $value, EXPECTED TYPE: $expectedType")
        return value?.takeIf { expectedType.isInstance(it) } ?: objectMapper.convertValue(value, expectedType)
    }

}
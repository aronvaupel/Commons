package com.ecommercedemo.common.controller.abstraction.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service

@Service
class SearchParamDeserializer(private val objectMapper: ObjectMapper) {

    fun convertAnyIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        println("SEARCHPARAMDESERIALIZER: VALUE: $value, EXPECTED TYPE: $expectedType")
        val result = when (value) {
            is Collection<*> -> {
                println("Collection detected by deserializer")
                value.map { element ->
                    println("SEARCHPARAMDESERIALIZER: ELEMENT: $element")
                    convertIfNeeded(element, expectedType) }
            }
            else -> convertIfNeeded(value, expectedType)
        }
        println("SEARCHPARAMDESERIALIZER: RESULT: $result")
        return result
    }

    private fun convertIfNeeded(value: Any?, expectedType: Class<*>): Any? {
        println("SEARCHPARAMDESERIALIZER:  Converting value")
        println("SEARCHPARAMDESERIALIZER:  VALUE: $value, EXPECTED TYPE: $expectedType")
        return value?.takeIf { expectedType.isInstance(it) } ?: objectMapper.convertValue(value, expectedType)
    }

}
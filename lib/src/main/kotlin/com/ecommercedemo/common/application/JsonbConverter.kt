package com.ecommercedemo.common.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
object JsonbConverter : AttributeConverter<Any, String> {
    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: Any?): String? {
        println("Start convertToDatabaseColumn. Attribute: $attribute")
        val result = attribute?.let { objectMapper.writeValueAsString(it) }
        println("End convertToDatabaseColumn. Result: $result")
        return result
    }

    override fun convertToEntityAttribute(dbData: String?): Any? {
        return dbData?.let { objectMapper.readValue(it, Any::class.java) }
    }
}
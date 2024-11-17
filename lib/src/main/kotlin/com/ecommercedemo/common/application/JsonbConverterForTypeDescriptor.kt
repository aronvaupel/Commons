package com.ecommercedemo.common.application

import com.ecommercedemo.common.application.search.TypeDescriptor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
object JsonbConverterForTypeDescriptor : AttributeConverter<TypeDescriptor, String> {
    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: TypeDescriptor?): String? {
        println("Start convertToDatabaseColumn. Attribute: $attribute")
        val result = attribute?.let { objectMapper.writeValueAsString(it) }
        println("End convertToDatabaseColumn. Result: $result")
        return result
    }

    override fun convertToEntityAttribute(dbData: String?): TypeDescriptor? {
        return dbData?.let { objectMapper.readValue(it, TypeDescriptor::class.java) }
    }
}
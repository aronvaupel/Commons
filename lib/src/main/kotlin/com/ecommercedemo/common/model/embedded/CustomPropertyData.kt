package com.ecommercedemo.common.model.embedded

import com.ecommercedemo.common.util.JsonbConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank

@Embeddable
@Suppress("unused")
class CustomPropertyData(
    @field:NotBlank(message = "Entity is mandatory")
    val entity: String,
    @field:NotBlank(message = "Key is mandatory")
    var key: String,
    @Convert(converter = JsonbConverter::class)
    var value: String? // Changed to String to avoid generics
) {
    constructor() : this("", "", null)

    inline fun <reified T> deserialize(): T? {
        return value?.let {
            val objectMapper = ObjectMapper()
            objectMapper.readValue(it, object : TypeReference<T>() {})
        }
    }

    companion object {
        inline fun <reified V : Any> serialize(entity: String, key: String, value: V?): CustomPropertyData {
            val jsonValue = JsonbConverter.convertToDatabaseColumn(value)
            return CustomPropertyData(entity, key, jsonValue)
        }
    }
}
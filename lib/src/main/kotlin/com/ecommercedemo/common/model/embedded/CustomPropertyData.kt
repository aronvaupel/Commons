package com.ecommercedemo.common.model.embedded

import com.ecommercedemo.common.util.JsonbConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Convert
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank

@Embeddable
@Suppress("unused")
class CustomPropertyData<V : Any>(
    @field:NotBlank(message = "Entity is mandatory")
    val entity: String,
    @field:NotBlank(message = "Key is mandatory")
    var key: String,
    @Convert(converter = JsonbConverter::class)
    var value: V?
) {
    constructor() : this("", "", null)

    inline fun <reified T> deserialize(): T? {
        return value?.let {
            val objectMapper = ObjectMapper()
            objectMapper.readValue(it as String, object : TypeReference<T>() {})
        }
    }

    companion object {
        inline fun <reified V : Any> serialize(entity: String, key: String, value: V?): CustomPropertyData<V> {
            val jsonValue = JsonbConverter.convertToDatabaseColumn(value)
            val objectMapper = ObjectMapper()
            val typedValue: V = objectMapper.readValue(jsonValue, object : TypeReference<V>() {})
            return CustomPropertyData(entity, key, typedValue)
        }
    }
}
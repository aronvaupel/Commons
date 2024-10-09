package com.ecommercedemo.common.model.embedded

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank

@Embeddable
@Suppress("unused")
class CustomPropertyData(
    @field:NotBlank(message = "Entity is mandatory")
    val entity: String,
    @field:NotBlank(message = "Key is mandatory")
    val key: String,
    val value: String?
) {
    constructor() : this("", "", null)

    inline fun <reified T> deserialize(): T? {
        return value?.let { ObjectMapper().readValue(it, T::class.java) }
    }

    companion object {
        fun <T> serialize(entity: String, key: String, value: T): CustomPropertyData {
            return CustomPropertyData(entity, key, ObjectMapper().writeValueAsString(value))
        }
    }

}
package com.ecommercedemo.model.embedded

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank
import kotlin.reflect.KClass

@Embeddable
@Suppress("unused")
class CustomProperty(
    @field:NotBlank(message = "Key is mandatory")
    val entity: String,
    @field:NotBlank(message = "Key is mandatory")
    val key: String,
    val value: String?
) {
    constructor() : this("", "", null)

    inline fun <reified T> deserialize(): T? {
        return value?.let { ObjectMapper().readValue(it, T::class.java) }
    }

    fun <T> serialize(key: String, value: T, entity: KClass<*>): CustomProperty {
        return CustomProperty(key, ObjectMapper().writeValueAsString(value), entity.qualifiedName ?: "")
    }

}
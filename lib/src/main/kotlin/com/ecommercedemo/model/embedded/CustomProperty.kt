package com.ecommercedemo.model.embedded

import jakarta.persistence.Embeddable
import jakarta.persistence.Column
import jakarta.validation.constraints.NotBlank
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper

@Embeddable
@JsonInclude(JsonInclude.Include.NON_NULL)
@Suppress("unused")
class CustomProperty<T>(
    @field:NotBlank(message = "Label is mandatory")
    @Column(name = "label")
    val label: String,

    @Column(name = "value", columnDefinition = "jsonb")
    val value: T?
) {
    constructor() : this("", null)

    inline fun <reified T> getValueAs(): T? {
        return value?.let { ObjectMapper().readValue(it.toString(), T::class.java) }
    }
}
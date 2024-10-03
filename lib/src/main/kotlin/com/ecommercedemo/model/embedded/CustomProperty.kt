package com.ecommercedemo.model.embedded

import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotBlank

@Embeddable
@Suppress("unused")
class CustomProperty<T>(
    @field:NotBlank(message = "Key is mandatory")
    val key: String,
    val value: T?
) {
    constructor() : this("", null)
}
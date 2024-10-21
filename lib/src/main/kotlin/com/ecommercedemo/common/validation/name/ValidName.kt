package com.ecommercedemo.userservice.validation.name

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kotlin.reflect.KClass

@NotBlank(message = "Name is mandatory")
@Size(max = 50, message = "Name must be less than 50 characters")
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NameValidator::class])
annotation class ValidName(
    val message: String = "Name contains invalid characters",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
package com.ecommercedemo.common.validation.name

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NameValidator::class])
annotation class ValidName(
    val message: String = "Name contains invalid characters",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
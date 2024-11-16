package com.ecommercedemo.common.application.validation.specialcharacters

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Size
import kotlin.reflect.KClass

@Size(max = 100, message = "Street must be less than 100 characters")
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ExcludeSpecialCharactersValidator::class])
annotation class ExcludeSpecialCharacters(
    val message: String = "Field contains invalid characters",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
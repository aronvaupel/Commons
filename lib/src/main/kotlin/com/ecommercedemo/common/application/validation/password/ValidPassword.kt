package com.ecommercedemo.common.application.validation.password

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass


@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PasswordValidator::class])
annotation class ValidPassword(
    val message: String = "Password must be at least 8 characters long and include an uppercase letter, a lowercase letter, a digit, and a special character",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

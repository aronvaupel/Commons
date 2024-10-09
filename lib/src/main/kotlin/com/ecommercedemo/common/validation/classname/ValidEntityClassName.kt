package com.ecommercedemo.common.validation.classname

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kotlin.reflect.KClass

@NotNull
@NotBlank
@MustBeDocumented
@Constraint(validatedBy = [EntityClassNameValidator::class])
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidEntityClassName(
    val message: String = "Invalid entity class name",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

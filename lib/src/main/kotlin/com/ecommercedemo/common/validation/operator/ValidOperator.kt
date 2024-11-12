package com.ecommercedemo.common.validation.operator

import jakarta.validation.Constraint
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [OperatorTypeValidator::class])
annotation class ValidOperator(
    val message: String = "The operator type does not match the parameter type.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)
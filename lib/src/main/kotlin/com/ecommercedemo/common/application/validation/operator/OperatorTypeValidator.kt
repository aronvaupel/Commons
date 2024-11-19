package com.ecommercedemo.common.application.validation.operator

import com.ecommercedemo.common.controller.abstraction.util.SearchParams
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object OperatorTypeValidator : ConstraintValidator<ValidOperator, SearchParams> {

    override fun isValid(searchParams: SearchParams, context: ConstraintValidatorContext): Boolean {
        val operator = searchParams.operator
        val value = searchParams.searchValue
        return value == null || operator.isSupportedType(value::class)
    }
}
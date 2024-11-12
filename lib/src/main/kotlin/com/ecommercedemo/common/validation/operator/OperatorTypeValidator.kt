package com.ecommercedemo.common.validation.operator

import com.ecommercedemo.common.util.search.dto.SearchParams
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object OperatorTypeValidator : ConstraintValidator<ValidOperator, SearchParams> {

    override fun isValid(searchParams: SearchParams, context: ConstraintValidatorContext): Boolean {
        val operator = searchParams.operator
        val value = searchParams.searchValue
        return operator.isSupportedType(value::class)
    }
}
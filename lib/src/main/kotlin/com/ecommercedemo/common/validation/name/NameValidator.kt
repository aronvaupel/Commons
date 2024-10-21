package com.ecommercedemo.userservice.validation.name

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object NameValidator : ConstraintValidator<ValidName, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true // Allow null values
        val allowedPattern = "^[a-zA-Z\\s\\-]*\$"
        return value.matches(allowedPattern.toRegex())
    }
}
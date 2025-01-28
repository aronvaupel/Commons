package com.ecommercedemo.common.application.validation.name

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object NameValidator : ConstraintValidator<ValidName, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) return false
        if (value.length > 50) return false
        val allowedPattern = "^[a-zA-Z\\s\\-'’éèêëàâäùûüîïç]*\$"
        return value.matches(allowedPattern.toRegex())
    }
}
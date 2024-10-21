package com.ecommercedemo.userservice.validation.specialcharacters

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object ExcludeSpecialCharactersValidator : ConstraintValidator<ExcludeSpecialCharacters, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true // Allow null values
        val allowedPattern = "^[a-zA-Z0-9\\s\\-\\,\\/\\.\\(\\)]*\$"
        return value.matches(allowedPattern.toRegex())
    }
}
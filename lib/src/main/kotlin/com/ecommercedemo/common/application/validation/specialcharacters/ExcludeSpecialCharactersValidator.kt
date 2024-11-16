package com.ecommercedemo.common.application.validation.specialcharacters

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object ExcludeSpecialCharactersValidator : ConstraintValidator<ExcludeSpecialCharacters, String?> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        val allowedPattern = Regex("^[a-zA-Z0-9\\s\\-,/.()]*$")
        return value.matches(allowedPattern)
    }
}
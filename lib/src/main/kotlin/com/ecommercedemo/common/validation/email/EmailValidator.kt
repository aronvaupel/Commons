package com.ecommercedemo.common.validation.email

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object EmailValidator : ConstraintValidator<ValidEmail, String> {

    override fun isValid(email: String?, context: ConstraintValidatorContext): Boolean {
        if (email.isNullOrBlank()) return false
        if (email.length > 100) return false

        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return email.matches(emailPattern)
    }
}
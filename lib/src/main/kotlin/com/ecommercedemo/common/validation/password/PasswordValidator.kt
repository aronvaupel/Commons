package com.ecommercedemo.common.validation.password

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object PasswordValidator : ConstraintValidator<ValidPassword, String> {
    override fun isValid(password: String?, context: ConstraintValidatorContext?): Boolean {
        if (password.isNullOrBlank()) return false
        if (password.length > 100) return false
        val passwordPattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!])(?=\\S+$).{8,}$")
        return password.matches(passwordPattern)
    }
}
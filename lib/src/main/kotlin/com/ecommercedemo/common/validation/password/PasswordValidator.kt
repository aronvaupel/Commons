package com.ecommercedemo.userservice.validation.password

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object PasswordValidator : ConstraintValidator<ValidPassword, String> {
    override fun isValid(password: String?, context: ConstraintValidatorContext?): Boolean {
        if (password == null) return false
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!])(?=\\S+\$).{8,}\$"
        return password.matches(passwordPattern.toRegex())
    }
}
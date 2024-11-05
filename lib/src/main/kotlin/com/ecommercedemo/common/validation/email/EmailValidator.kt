package com.ecommercedemo.common.validation.email

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EmailValidator : ConstraintValidator<ValidEmail, String> {

    override fun isValid(email: String?, context: ConstraintValidatorContext): Boolean {
        if (email.isNullOrBlank()) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("Email must not be blank")
                .addConstraintViolation()
            return false
        }

        if (email.length > 100) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("Email must be less than 100 characters")
                .addConstraintViolation()
            return false
        }

        val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        if (!email.matches(emailPattern)) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("Email must be a valid format")
                .addConstraintViolation()
            return false
        }

        return true
    }
}
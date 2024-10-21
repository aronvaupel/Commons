package com.ecommercedemo.userservice.validation.phone

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object PhoneValidator : ConstraintValidator<ValidPhone, String?> {
    override fun isValid(phoneNumber: String?, context: ConstraintValidatorContext): Boolean {
        if (phoneNumber == null) return true // Allow null values
        val phonePattern = "^\\+?[1-9]\\d{1,14}\$"
        return phoneNumber.matches(phonePattern.toRegex())
    }
}
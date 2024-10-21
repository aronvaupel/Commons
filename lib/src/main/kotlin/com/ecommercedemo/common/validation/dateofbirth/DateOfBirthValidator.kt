package com.ecommercedemo.userservice.validation.dateofbirth

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate

object DateOfBirthValidator : ConstraintValidator<ValidDateOfBirth, LocalDate> {

    override fun isValid(value: LocalDate?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return false
        val today = LocalDate.now()
        return value.isBefore(today) && value.isAfter(today.minusYears(120))
    }
}
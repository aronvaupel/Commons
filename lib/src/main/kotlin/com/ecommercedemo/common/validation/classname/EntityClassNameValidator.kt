package com.ecommercedemo.common.validation.classname

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object EntityClassNameValidator : ConstraintValidator<ValidEntityClassName, String> {

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return try {
            Class.forName(value)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
package com.ecommercedemo.common.validation.classname

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

object EntityClassNameValidator : ConstraintValidator<ValidEntityClassName, String> {

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return try {
            val className = Class.forName(value).simpleName
            className == value
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
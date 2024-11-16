package com.ecommercedemo.common.application.validation.classname

import com.ecommercedemo.common.application.EntityScanner
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EntityClassNameValidator @Autowired constructor(
    private val entityScanner: EntityScanner
) : ConstraintValidator<ValidEntityClassName, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value != null && entityScanner.getUpstreamEntityNames().contains(value)
    }
}
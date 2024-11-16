package com.ecommercedemo.common.application.validation.classname

import com.ecommercedemo.common.application.EntityScanner
import jakarta.persistence.EntityManagerFactory
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EntityClassNameValidator @Autowired constructor(
    private val entityScanner: EntityScanner
) : ConstraintValidator<ValidEntityClassName, String> {

    constructor() : this(EntityScanner(
        entityManagerFactory = EntityManagerFactory::class.java.getDeclaredConstructor().newInstance()
    ))

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return  entityScanner.getUpstreamEntityNames().contains(value)
    }
}
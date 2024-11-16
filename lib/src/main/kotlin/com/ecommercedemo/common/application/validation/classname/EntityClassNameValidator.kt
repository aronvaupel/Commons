package com.ecommercedemo.common.application.validation.classname

import jakarta.persistence.EntityManager
import jakarta.persistence.metamodel.EntityType
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.springframework.beans.factory.annotation.Autowired

class EntityClassNameValidator : ConstraintValidator<ValidEntityClassName, String> {

    @Autowired
    private lateinit var entityManager: EntityManager

    private lateinit var validClassNames: Set<String>

    override fun initialize(constraintAnnotation: ValidEntityClassName?) {
        validClassNames = entityManager.metamodel.entities
            .map(EntityType<*>::getJavaType)
            .map(Class<*>::getSimpleName)
            .toSet()
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) return false
        return validClassNames.contains(value)
    }
}
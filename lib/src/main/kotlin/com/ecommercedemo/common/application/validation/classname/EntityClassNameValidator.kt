package com.ecommercedemo.common.application.validation.classname

import jakarta.persistence.Persistence
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext


class EntityClassNameValidator : ConstraintValidator<ValidEntityClassName, String> {

    private lateinit var entityNames: Set<String>

    override fun initialize(constraintAnnotation: ValidEntityClassName) {
        val entityManagerFactory = Persistence.createEntityManagerFactory("default")
        entityNames = entityManagerFactory.metamodel.entities
            .map { it.javaType.simpleName }
            .toSet()
        entityManagerFactory.close()
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return false
        return entityNames.contains(value)
    }
}
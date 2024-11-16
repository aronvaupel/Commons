package com.ecommercedemo.common.application.validation.classname

import jakarta.persistence.EntityManager
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.hibernate.validator.internal.util.stereotypes.Lazy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EntityClassNameValidator: ConstraintValidator<ValidEntityClassName, String> {
    @Autowired
    @Lazy
    private lateinit var entityManager: EntityManager

    override fun isValid(value: String, context: ConstraintValidatorContext?): Boolean {
        return try {
            val className = entityManager.metamodel.entities
                .find { it.javaType.simpleName == value }
                ?.javaType?.simpleName
            className == value
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
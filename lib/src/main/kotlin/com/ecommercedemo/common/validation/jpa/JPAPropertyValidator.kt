package com.ecommercedemo.common.validation.jpa

import com.ecommercedemo.common.exception.InvalidAttributeException
import com.ecommercedemo.common.exception.ValueTypeMismatchException
import com.ecommercedemo.common.util.filter.FilterCriteria
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class JPAPropertyValidator {

    fun <T : Any> validateAttribute(entityClass: KClass<T>, filter: FilterCriteria) {
        val attribute = getAttributeProperty(entityClass, filter.jpaAttribute)
        validateAttributeType(attribute, filter.value, filter.jpaAttribute)
    }

    private fun <T : Any> getAttributeProperty(entityClass: KClass<T>, attributeName: String): KProperty1<T, *> {
        return entityClass.memberProperties.find { it.name == attributeName }
            ?: throw InvalidAttributeException(attributeName, entityClass.simpleName ?: "UnknownEntity")
    }

    private fun <T> validateAttributeType(attribute: KProperty1<T, *>, value: Any?, attributeName: String) {
        val expectedType = attribute.returnType.classifier as? KClass<*>
        val actualType = value?.let { it::class }

        if (value != null && expectedType != null && !expectedType.isInstance(value)) {
            throw ValueTypeMismatchException(attributeName, expectedType.simpleName ?: "UnknownType", actualType?.simpleName ?: "UnknownType")
        }
    }
}
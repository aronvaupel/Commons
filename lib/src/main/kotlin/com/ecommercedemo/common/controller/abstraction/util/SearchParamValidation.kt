package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.exception.InvalidAttributeException
import com.ecommercedemo.common.application.exception.ValueTypeMismatchException
import com.ecommercedemo.common.service.CachingEligible
import com.ecommercedemo.common.service.concretion.ReflectionService
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
@Suppress("unused")
class SearchParamValidation(
    private val deserializer: SearchParamConverter,
    private val reflectionService: ReflectionService,
) {
    @CachingEligible
    fun validate(value: Any?, expectedType: Class<*>, declaringClass: KClass<*>, attributePath: String) {
        if (value == null && reflectionService.getClassMemberProperties(declaringClass)
                .find { it.name == attributePath }
                ?.returnType
                ?.isMarkedNullable == true
        ) {
            throw ValueTypeMismatchException(
                attributePath = attributePath,
                expectedType = expectedType.simpleName ?: "Unknown",
                actualType = "null"
            )
        }

        if (value is Collection<*>) {
            validateCollectionElements(value, expectedType, attributePath)
        } else if (!expectedType.isInstance(value)) {
            throw ValueTypeMismatchException(
                attributePath = attributePath,
                expectedType = expectedType.simpleName ?: "Unknown",
                actualType = value?.let { value::class.simpleName ?: "Unknown" } ?: "null"
            )
        }
    }

    @CachingEligible
    private fun validateCollectionElements(collection: Collection<*>, expectedType: Class<*>, attributePath: String) {
        collection.forEach { element ->
            if (element != null && !expectedType.isInstance(element)) {
                throw ValueTypeMismatchException(
                    attributePath = attributePath,
                    expectedType = expectedType.simpleName ?: "Unknown",
                    actualType = element::class.simpleName ?: "Unknown"
                )
            }
        }
    }

    @CachingEligible
    fun validateFieldExistsAndIsAccessible(segment: String, currentClass: Class<*>) {
        var classToCheck: Class<*>? = currentClass

        while (classToCheck != null) {
            try {
                val field = classToCheck.getDeclaredField(segment)
                field.isAccessible = true
                return
            } catch (e: NoSuchFieldException) {
                classToCheck = classToCheck.superclass
            }
        }

        throw InvalidAttributeException(segment, currentClass.simpleName)
    }
}

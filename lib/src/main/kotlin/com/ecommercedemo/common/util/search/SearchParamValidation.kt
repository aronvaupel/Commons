package com.ecommercedemo.common.util.search

import com.ecommercedemo.common.exception.InvalidAttributeException
import com.ecommercedemo.common.exception.ValueTypeMismatchException
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Service
@Suppress("unused")
class SearchParamValidation(
    private val deserializer: SearchParamDeserializer
) {
    fun validate(value: Any?, expectedType: Class<*>, declaringClass: KClass<*>, attributePath: String) {
        println("Start validating value")
        if (value == null && declaringClass.memberProperties
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
            println("Collection detected by validator")
            validateCollectionElements(value, expectedType, attributePath)
        } else if (!expectedType.isInstance(value)) {
            throw ValueTypeMismatchException(
                attributePath = attributePath,
                expectedType = expectedType.simpleName ?: "Unknown",
                actualType = value?.let { value::class.simpleName ?: "Unknown" } ?: "null"
            )
        }
    }

    private fun validateCollectionElements(collection: Collection<*>, expectedType: Class<*>, attributePath: String) {
        println("Start validating collection elements")
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

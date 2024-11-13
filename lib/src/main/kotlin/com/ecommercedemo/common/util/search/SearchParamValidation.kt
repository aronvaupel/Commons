package com.ecommercedemo.common.util.search

import com.ecommercedemo.common.exception.InvalidAttributeException
import com.ecommercedemo.common.exception.ValueTypeMismatchException
import jakarta.persistence.criteria.Path
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Service
class SearchParamValidation(
    private val deserializer: SearchParamDeserializer
) {
    fun validate(value: Any?, expectedType: Class<*>, fieldName: String, declaringClass: KClass<*>, attributePath: String) {
        if (value == null && declaringClass.memberProperties
                .find { it.name == fieldName }
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

    fun validateFinalSegmentType(path: Path<*>, value: Any?, fieldName: String, currentClass: KClass<*>) {
        val expectedType = path.model.bindableJavaType
        validate(value, expectedType, fieldName, currentClass, path.toString())
        val actualValue = deserializer.deserializeIfNeeded(value, expectedType)
        if (!expectedType.isInstance(actualValue)) {
            throw ValueTypeMismatchException(
                attributePath = path.toString(),
                expectedType = expectedType.simpleName ?: "Unknown",
                actualType = actualValue?.javaClass?.simpleName ?: "Unknown"
            )
        }
    }
}

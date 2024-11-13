package com.ecommercedemo.common.util.search

import com.ecommercedemo.common.exception.InvalidAttributeException
import com.ecommercedemo.common.exception.ValueTypeMismatchException
import com.ecommercedemo.common.model.BaseEntity
import com.ecommercedemo.common.util.search.dto.ResolvedPathInfo
import com.ecommercedemo.common.util.search.dto.SearchParams
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


@Service
class PathResolver(
    private val objectMapper: ObjectMapper = ObjectMapper().configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        true
    )
) {
    fun <T : BaseEntity> resolvePath(params: SearchParams, root: Root<T>): ResolvedPathInfo {
        val segments = params.path.split(".")
        var currentPath: Path<*> = root
        var currentClass: Class<*> = root.javaType

        segments.forEachIndexed { index, segment ->
            validateFieldExistsAndIsAccessible(segment, currentClass)
            if (isPseudoProperty(segment)) {
                val jsonSegments = segments.drop(index + 1)
                return ResolvedPathInfo(jpaPath = currentPath.get<Any>(segment), jsonSegments = jsonSegments)
            } else {
                currentPath = currentPath.get<Any>(segment)
                currentClass = currentPath.model.bindableJavaType
            }
        }

        validateFinalSegmentType(currentPath, params.searchValue, segments.last(), currentClass.kotlin)

        return ResolvedPathInfo(jpaPath = currentPath, jsonSegments = emptyList())
    }

    private fun validateFieldExistsAndIsAccessible(segment: String, currentClass: Class<*>) {
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

    private fun isPseudoProperty(segment: String) = segment == "pseudoProperties"


    private fun validateFinalSegmentType(path: Path<*>, value: Any?, fieldName: String, currentClass: KClass<*>) {
        val expectedType = path.model.bindableJavaType
        if (value == null && currentClass.memberProperties
                .find { it.name == fieldName }
                ?.returnType
                ?.isMarkedNullable == true
        ) {
            throw ValueTypeMismatchException(
                attributePath = path.toString(),
                expectedType = expectedType.simpleName ?: "Unknown",
                actualType = "null"
            )
        }
        val actualType = value?.takeIf { expectedType.isInstance(it) } ?: objectMapper.convertValue(value, expectedType)

        if (!expectedType.isInstance(actualType)) {
            throw ValueTypeMismatchException(
                attributePath = path.toString(),
                expectedType = expectedType.simpleName ?: "Unknown",
                actualType = actualType?.let { actualType::class.simpleName ?: "Unknown" } ?: "null"
            )
        }
    }
}

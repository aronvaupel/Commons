package com.ecommercedemo.common.util.search

import com.ecommercedemo.common.exception.InvalidAttributeException
import com.ecommercedemo.common.exception.ValueTypeMismatchException
import com.ecommercedemo.common.model.BaseEntity
import com.ecommercedemo.common.util.search.dto.ResolvedPathInfo
import com.ecommercedemo.common.util.search.dto.SearchParams
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Service


@Service
class PathResolver {
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

        validateFinalSegmentType(currentPath, params.searchValue)

        return ResolvedPathInfo(jpaPath = currentPath, jsonSegments = emptyList())
    }

    private fun validateFieldExistsAndIsAccessible(segment: String, currentClass: Class<*>) {
        try {
            currentClass.getDeclaredField(segment).isAccessible = true
        } catch (e: NoSuchFieldException) {
            throw InvalidAttributeException(segment, currentClass.simpleName)
        }
    }

    private fun isPseudoProperty(segment: String) = segment == "pseudoProperties"


    private fun validateFinalSegmentType(path: Path<*>, value: Any) {
        val expectedType = path.model.bindableJavaType
        val actualType = value::class.java

        if (expectedType != actualType) {
            throw ValueTypeMismatchException(
                attributePath = path.toString(),
                expectedType = expectedType.simpleName,
                actualType = actualType.simpleName
            )
        }
    }
}
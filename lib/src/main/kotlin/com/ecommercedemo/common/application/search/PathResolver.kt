package com.ecommercedemo.common.application.search

import com.ecommercedemo.common.application.search.dto.ResolvedSearchParam
import com.ecommercedemo.common.application.search.dto.SearchParams
import com.ecommercedemo.common.model.BaseEntity
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Service


@Service
class PathResolver(
    private val validator: SearchParamValidation,
    private val deserializer: SearchParamDeserializer,

) {
    fun <T : BaseEntity> resolvePath(params: SearchParams, root: Root<T>): ResolvedSearchParam {
        println("Resolving path: ${params.path}")
        val segments = params.path.split(".")
        var currentPath: Path<*> = root
        var currentClass: Class<*> = root.javaType

        segments.forEachIndexed { index, segment ->
            validator.validateFieldExistsAndIsAccessible(segment, currentClass)
            if (segment == "pseudoProperties") {
                val jsonSegments = segments.drop(index + 1)
                val actualValue = deserializer.convertAnyIfNeeded(params.searchValue, currentClass)
                return ResolvedSearchParam(actualValue, jpaPath = currentPath.get<Any>(segment), jsonSegments = jsonSegments)
            } else {
                currentPath = currentPath.get<Any>(segment)
                currentClass = currentPath.model.bindableJavaType
            }
        }
        val actualValue = deserializer.convertAnyIfNeeded(params.searchValue, currentClass)
        validator.validate(actualValue, currentPath.model.bindableJavaType, currentClass.kotlin, currentPath.toString())
        println("Finished resolving path: ${params.path}")
        return ResolvedSearchParam(actualValue, jpaPath = currentPath, jsonSegments = emptyList())
    }

}

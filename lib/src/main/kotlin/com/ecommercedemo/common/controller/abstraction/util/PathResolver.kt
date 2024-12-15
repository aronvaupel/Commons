package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
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
        println("SEGMENTS: $segments")
        var currentPath: Path<*> = root
        println("CURRENT PATH: $currentPath")
        var currentClass: Class<*> = root.javaType
        println("CURRENT CLASS: $currentClass")

        segments.forEachIndexed { index, segment ->
            println("SEGMENT: $segment")
            validator.validateFieldExistsAndIsAccessible(segment, currentClass)
            if (segment == AugmentableBaseEntity::pseudoProperties.name) {
                val jsonSegments = segments.drop(index + 1)
                println("JSON SEGMENTS: $jsonSegments")
                currentClass = currentPath.get<Any>(segment).model.bindableJavaType
                println("CURRENT CLASS FOR SEGMENT: $currentClass")
                val actualValue = deserializer.convertAnyIfNeeded(params.searchValue, currentClass)
                println("ACTUAL VALUE: $actualValue")
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

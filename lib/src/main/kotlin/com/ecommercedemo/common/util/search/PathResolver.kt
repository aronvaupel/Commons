package com.ecommercedemo.common.util.search

import com.ecommercedemo.common.model.BaseEntity
import com.ecommercedemo.common.util.search.dto.ResolvedPathInfo
import com.ecommercedemo.common.util.search.dto.SearchParams
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Service


@Service
class PathResolver(
    private val validator: SearchParamValidation

) {
    fun <T : BaseEntity> resolvePath(params: SearchParams, root: Root<T>): ResolvedPathInfo {
        println("Resolving path: ${params.path}")
        val segments = params.path.split(".")
        var currentPath: Path<*> = root
        var currentClass: Class<*> = root.javaType

        segments.forEachIndexed { index, segment ->
            validator.validateFieldExistsAndIsAccessible(segment, currentClass)
            if (segment == "pseudoProperties") {
                val jsonSegments = segments.drop(index + 1)
                return ResolvedPathInfo(jpaPath = currentPath.get<Any>(segment), jsonSegments = jsonSegments)
            } else {
                currentPath = currentPath.get<Any>(segment)
                currentClass = currentPath.model.bindableJavaType
            }
        }

        validator.validateFinalSegmentType(currentPath, params.searchValue, segments.last(), currentClass.kotlin)

        return ResolvedPathInfo(jpaPath = currentPath, jsonSegments = emptyList())
    }

}

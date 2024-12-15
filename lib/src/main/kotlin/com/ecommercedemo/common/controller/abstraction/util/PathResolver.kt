package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.concretion._pseudoProperty._PseudoPropertyRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Service


@Service
class PathResolver(
    private val validator: SearchParamValidation,
    private val deserializer: SearchParamDeserializer,
    private val _pseudoPropertyRepository: _PseudoPropertyRepository,
    private val objectMapper: ObjectMapper
) {
    fun <T : BaseEntity> resolvePath(params: SearchParams, root: Root<T>): ResolvedSearchParam {
        println("PATHRESOVER: Resolving path: ${params.path}")
        val segments = params.path.split(".")
        var currentPath: Path<*> = root
        var currentClass: Class<*> = root.javaType

        val registeredPseudoPropertyTypesMap =
            _pseudoPropertyRepository.findAllByEntitySimpleName(currentClass.simpleName)
                .associate { it.key to it.typeDescriptor.type.typeInfo }

        segments.forEachIndexed { index, segment ->
            validator.validateFieldExistsAndIsAccessible(segment, currentClass)
            if (segment == AugmentableBaseEntity::pseudoProperties.name) {
                val jsonSegments = segments.drop(index + 1)
                val relevantSegment = jsonSegments.find { jsonSegment ->
                    jsonSegment == params.path.substringAfterLast(".")
                } ?: throw IllegalArgumentException("PseudoProperty not found")

                val segmentValue = objectMapper.writeValueAsString(mapOf(relevantSegment to params.searchValue))

                val result = ResolvedSearchParam(
                    deserializedValue = segmentValue,
                    jpaPath = currentPath.get<Any>(segment),
                    jsonSegments = jsonSegments
                )
                println("RESULT $result")
                return result
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

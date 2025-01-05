package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.concretion._pseudoProperty._PseudoPropertyRepository
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.criteria.Path
import jakarta.persistence.criteria.Root
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class PathResolver(
    private val validator: SearchParamValidation,
    private val converter: SearchParamConverter,
    @Autowired(required = false) private val _pseudoPropertyRepository: _PseudoPropertyRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.application.name}") private val applicationName: String
) {
    fun <T : BaseEntity> resolvePath(params: SearchParam, root: Root<T>): ResolvedSearchParam {
        val segments = params.path.split(".")
        var currentPath: Path<*> = root
        var currentClass: Class<*> = root.javaType

        val registeredPseudoPropertyTypesMap =
            if (applicationName != "pseudoproperty-service") _pseudoPropertyRepository.findAllByEntitySimpleName(
                currentClass.simpleName
            )
                .associate { it.key to it.typeDescriptor.type.typeInfo } else null

        segments.forEachIndexed { index, segment ->
            validator.validateFieldExistsAndIsAccessible(segment, currentClass)
            if (segment == AugmentableBaseEntity::pseudoProperties.name) {
                val jsonSegments = segments.drop(index + 1)

                val relevantSegment = jsonSegments.find { jsonSegment ->
                    jsonSegment == params.path.substringAfterLast(".")
                } ?: throw IllegalArgumentException("PseudoProperty not found")

                val expectedType = registeredPseudoPropertyTypesMap?.get(relevantSegment)
                    ?: throw IllegalArgumentException("PseudoProperty type not found")

                val rawSegmentValue = converter.convertAnyIfNeeded(params.searchValue, expectedType)

                val serializedSegmentValue = when (rawSegmentValue) {
                    is String -> "\"$rawSegmentValue\""
                    null -> null
                    else -> objectMapper.writeValueAsString(rawSegmentValue)
                }

                val result = ResolvedSearchParam(
                    deserializedValue = serializedSegmentValue,
                    jpaPath = currentPath.get<Any>(segment),
                    jsonSegments = jsonSegments
                )
                return result
            } else {
                currentPath = currentPath.get<Any>(segment)
                currentClass = currentPath.model.bindableJavaType
            }
        }
        val actualValue = converter.convertAnyIfNeeded(params.searchValue, currentClass)
        validator.validate(actualValue, currentPath.model.bindableJavaType, currentClass.kotlin, currentPath.toString())
        return ResolvedSearchParam(actualValue, jpaPath = currentPath, jsonSegments = emptyList())
    }

}

package com.ecommercedemo.common.util.search

import com.ecommercedemo.common.model.BaseEntity
import com.ecommercedemo.common.util.search.dto.SearchRequest
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class Retriever(
    private val entityManager: EntityManager,
    private val pathResolver: PathResolver,
    private val objectMapper: ObjectMapper = ObjectMapper().configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        true
    )
) {

    fun <T : BaseEntity> executeSearch(searchRequest: SearchRequest, entity: KClass<T>): List<T> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(entity.java)
        val root = criteriaQuery.from(entity.java)

        val predicates = searchRequest.params.map { param ->
            val resolvedPathInfo = pathResolver.resolvePath(param, root)

            if (resolvedPathInfo.jsonSegments.isNotEmpty()) {
               //Todo: Check if deserialization is needed once pseudo properties and redis is implemented
                criteriaBuilder.isTrue(
                    param.operator.buildCondition(resolvedPathInfo, criteriaBuilder, param.searchValue)
                )
            } else {
                val expectedValueType = resolvedPathInfo.jpaPath.model.bindableJavaType

                val actualValue = when (param.searchValue) {
                    is Collection<*> -> {
                        param.searchValue.map { validateAndConvert(it, expectedValueType) }
                    }

                    is Pair<*, *> -> {
                        val (first, second) = param.searchValue
                        Pair(validateAndConvert(first, expectedValueType), validateAndConvert(second, expectedValueType))
                    }

                    else -> param.searchValue?.takeIf { expectedValueType.isInstance(it) }
                        ?: objectMapper.convertValue(param.searchValue, expectedValueType)
                }
                param.operator.buildPredicate(criteriaBuilder, resolvedPathInfo.jpaPath, actualValue)
            }
        }

        criteriaQuery.where(*predicates.toTypedArray())

        return entityManager.createQuery(criteriaQuery).resultList
    }

    private fun validateAndConvert(value: Any?, expectedValueType: Class<*>): Any? {
        require(value != null && expectedValueType.isInstance(value)) {
            "All elements in searchValue must match expected type $expectedValueType and cannot be null for comparisons."
        }
        return value.takeIf { expectedValueType.isInstance(it) }
            ?: objectMapper.convertValue(value, expectedValueType)
    }

}

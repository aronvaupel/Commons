package com.ecommercedemo.common.application.search

import com.ecommercedemo.common.application.search.dto.SearchRequest
import com.ecommercedemo.common.model.BaseEntity
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
@Suppress("unused")
class Retriever(
    private val deserializer: SearchParamDeserializer,
    private val entityManager: EntityManager,
    private val pathResolver: PathResolver,
    private val validator: SearchParamValidation
) {

    fun <T : BaseEntity> executeSearch(searchRequest: SearchRequest, entity: KClass<T>): List<T> {
        println("Start executing search")
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(entity.java)
        val root = criteriaQuery.from(entity.java)

        val predicates = searchRequest.params.map { param ->
            val resolvedPathInfo = pathResolver.resolvePath(param, root)
            val deserializedValue = resolvedPathInfo.deserializedValue

            if (resolvedPathInfo.jsonSegments.isNotEmpty()) {
               //Todo: Check if deserialization is needed once pseudo properties and redis is implemented
                criteriaBuilder.isTrue(
                    param.operator.buildCondition(resolvedPathInfo, criteriaBuilder, param.searchValue)
                )
            } else {
                param.operator.buildPredicate(criteriaBuilder, resolvedPathInfo.jpaPath, deserializedValue)
            }
        }

        criteriaQuery.where(*predicates.toTypedArray())

        return entityManager.createQuery(criteriaQuery).resultList
    }

}
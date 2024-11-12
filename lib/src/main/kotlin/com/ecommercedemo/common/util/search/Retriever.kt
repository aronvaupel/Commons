package com.ecommercedemo.common.util.search

import com.ecommercedemo.common.model.BaseEntity
import com.ecommercedemo.common.util.search.dto.SearchRequest
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class Retriever(
    private val entityManager: EntityManager,
    private val pathResolver: PathResolver
) {

    fun <T : BaseEntity> executeSearch(searchRequest: SearchRequest, entity: KClass<T>): List<T> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(entity.java)
        val root = criteriaQuery.from(entity.java)

        val predicates = searchRequest.params.map { param ->

            val resolvedPathInfo = pathResolver.resolvePath(param, root)
            if (resolvedPathInfo.jsonSegments.isNotEmpty()) {
                criteriaBuilder.isTrue(
                    param.operator.buildCondition(resolvedPathInfo, criteriaBuilder, param.searchValue)
                )
            } else {
                param.operator.buildPredicate(criteriaBuilder, resolvedPathInfo.jpaPath, param.searchValue)
            }
        }

        criteriaQuery.where(*predicates.toTypedArray())

        return entityManager.createQuery(criteriaQuery).resultList
    }
}

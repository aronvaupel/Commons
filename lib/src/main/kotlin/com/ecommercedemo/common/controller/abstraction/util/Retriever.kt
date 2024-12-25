package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
@Suppress("unused")
class Retriever(
    private val deserializer: SearchParamConverter,
    private val entityManager: EntityManager,
    private val pathResolver: PathResolver,
    private val redisService: RedisService,
    private val validator: SearchParamValidation
) {

    fun <T : BaseEntity> executeSearch(searchRequest: SearchRequest, entity: KClass<T>, page: Int, size: Int): Page<T> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(entity.java)
        val root = criteriaQuery.from(entity.java)

        val predicates = searchRequest.params.map { param ->
            val resolvedPathInfo = pathResolver.resolvePath(param, root)
            val deserializedValue = resolvedPathInfo.deserializedValue

            if (resolvedPathInfo.jsonSegments.isNotEmpty()) {
                criteriaBuilder.isTrue(
                    param.operator.buildCondition(resolvedPathInfo, criteriaBuilder)
                )
            } else {
                param.operator.buildPredicate(criteriaBuilder, resolvedPathInfo.jpaPath, deserializedValue)
            }
        }

        criteriaQuery.where(*predicates.toTypedArray())

        val countQuery = criteriaBuilder.createQuery(Long::class.java).apply {
            select(criteriaBuilder.count(root))
            where(*predicates.toTypedArray())
        }
        val totalCount = entityManager.createQuery(countQuery).singleResult

        val query = entityManager.createQuery(criteriaQuery)
            .setFirstResult(page * size)
            .setMaxResults(size)

        val resultList = query.resultList

        return PageImpl(resultList, PageRequest.of(page, size), totalCount)
    }

}

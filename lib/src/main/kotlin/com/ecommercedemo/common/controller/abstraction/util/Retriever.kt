package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.Tuple
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*
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
    val log = KotlinLogging.logger {}

    fun <T : BaseEntity> executeSearch(searchRequest: SearchRequest, entity: KClass<T>, page: Int, size: Int): Page<T> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(Tuple::class.java)
        val root = criteriaQuery.from(entity.java)
        val pageable = PageRequest.of(page, size)
        log.info("Pageable info: page = {}, size = {}", pageable.pageNumber, pageable.pageSize)


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

        val entityAlias = "entity"
        val countAlias = "totalCount"

        criteriaQuery.multiselect(
            root.alias(entityAlias),
            criteriaBuilder.count(root).alias(countAlias)
        )
        criteriaQuery.groupBy(root.get<UUID>("id"))
        criteriaQuery.where(*predicates.toTypedArray())

        val resultList = entityManager.createQuery(criteriaQuery)
            .setFirstResult(pageable.offset.toInt())
            .setMaxResults(pageable.pageSize)
            .resultList


        val entities = resultList.map { it.get("entity", entity.java) }
        val totalCount = resultList.firstOrNull()?.get("totalCount", Long::class.java) ?: 0L

        return PageImpl(entities, pageable, totalCount)
    }

}

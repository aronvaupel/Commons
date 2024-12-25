package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.model.abstraction.BaseEntity
import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Root
import org.springframework.stereotype.Service
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
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

//    fun <T : BaseEntity> executeSearch(searchRequest: SearchRequest, entity: KClass<T>): List<T> {
//        val criteriaBuilder = entityManager.criteriaBuilder
//        val criteriaQuery = criteriaBuilder.createQuery(entity.java)
//        val root = criteriaQuery.from(entity.java)
//        var fullResult = mutableListOf<T>()
//
//         searchRequest.params.forEach { param ->
//            val resolvedPathInfo = pathResolver.resolvePath(param, root)
//            val deserializedValue = resolvedPathInfo.deserializedValue
//
//            val predicate = if (resolvedPathInfo.jsonSegments.isNotEmpty()) {
//                criteriaBuilder.isTrue(
//                    param.operator.buildCondition(resolvedPathInfo, criteriaBuilder)
//                )
//            } else {
//                param.operator.buildPredicate(criteriaBuilder, resolvedPathInfo.jpaPath, deserializedValue)
//            }
//            val query = criteriaQuery.where(predicate)
//            val partialResult = entityManager.createQuery(query).resultList.toMutableList()
//             redisService.cachePartialSearchResult(entity.simpleName!!, param, partialResult.map { it.id })
//             fullResult =
//                if (fullResult.isEmpty()) partialResult
//                else fullResult.intersect(partialResult.toSet()).toMutableList()
//        }
//        return fullResult
//    }

    fun <T : BaseEntity> executeSearch(searchRequest: SearchRequest, entity: KClass<T>): List<T> {
        val criteriaBuilder = entityManager.criteriaBuilder
        val criteriaQuery = criteriaBuilder.createQuery(entity.java)
        val root = criteriaQuery.from(entity.java)

        if (searchRequest.params.size == 1) {
            val param = searchRequest.params.first()
            return processSingleSearchParam(param, criteriaBuilder, criteriaQuery, root, entity)
        }

        val optimalThreads = determineOptimalThreadCount()
        val threadPool = Executors.newFixedThreadPool(optimalThreads)
        val futures = mutableListOf<Future<List<T>>>()

        searchRequest.params.forEach { param ->
            futures.add(threadPool.submit(Callable {
                processSingleSearchParam(param, criteriaBuilder, criteriaQuery, root, entity)
            }))
        }

        val results = mutableListOf<T>()
        try {
            val partialResults = futures.map { it.get() }
            if (partialResults.isNotEmpty()) {
                results.addAll(partialResults.reduce { acc, list -> acc.intersect(list.toSet()).toMutableList() })
            }
        } finally {
            threadPool.shutdown()
        }

        return results
    }

    private fun <T : BaseEntity> processSingleSearchParam(
        param: SearchParam,
        criteriaBuilder: CriteriaBuilder,
        criteriaQuery: CriteriaQuery<T>,
        root: Root<T>,
        entity: KClass<T>
    ): List<T> {
        val resolvedPathInfo = pathResolver.resolvePath(param, root)
        val deserializedValue = resolvedPathInfo.deserializedValue

        val predicate = if (resolvedPathInfo.jsonSegments.isNotEmpty()) {
            criteriaBuilder.isTrue(
                param.operator.buildCondition(resolvedPathInfo, criteriaBuilder)
            )
        } else {
            param.operator.buildPredicate(criteriaBuilder, resolvedPathInfo.jpaPath, deserializedValue)
        }

        val query = criteriaQuery.where(predicate)
        val partialResult = entityManager.createQuery(query).resultList.toMutableList()

        redisService.cachePartialSearchResult(entity.simpleName!!, param, partialResult.map { it.id })

        return partialResult
    }

    private fun determineOptimalThreadCount(): Int {
        val cores = Runtime.getRuntime().availableProcessors()
        val waitTimeFraction = 0.5 // Adjust based on workload (e.g., I/O-heavy tasks)
        return (cores * (1 + waitTimeFraction)).toInt()
    }

}

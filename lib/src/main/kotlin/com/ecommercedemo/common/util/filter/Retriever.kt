package com.ecommercedemo.common.util.filter

import com.ecommercedemo.common.model.BaseEntity
import com.ecommercedemo.common.redis.RedisService
import org.springframework.stereotype.Service

@Service
class Retriever(
    private val redisService: RedisService,
    private val jpaQueryProcessor: JPAQueryProcessor,
    private val jsonBQueryProcessor: JsonBQueryProcessor
) {

    fun <T : BaseEntity> retrieve(queryParams: QueryParams<T>, entity: Class<T>): List<T> {
        val cacheKey = redisService.generateQueryKey(entity, queryParams)
        val cachedResult: List<T>? = redisService.getQueryResult(cacheKey) as? List<T>

        if (cachedResult != null) {
            return cachedResult
        }

        val results = mutableListOf<T>()

        queryParams.filters.forEach { filter ->
            if (filter.jpaAttribute == "pseudoProperties") {
                val processorResults = jsonBQueryProcessor.process(filter, entity)
                results.addAll(processorResults)
            } else {
                val processorResults = jpaQueryProcessor.process(filter, entity)
                results.addAll(processorResults)
            }
        }
        val uniqueResults = results.distinct()
        redisService.cacheQueryResult(cacheKey, uniqueResults)

        return uniqueResults
    }
}


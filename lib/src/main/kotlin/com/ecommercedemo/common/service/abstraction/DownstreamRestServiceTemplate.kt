package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.application.exception.NotCachedException
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.PersistencePort
import com.ecommercedemo.common.service.annotation.RestServiceFor
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Suppress("UNCHECKED_CAST")
abstract class DownstreamRestServiceTemplate<T : BaseEntity> : IDownstreamRestService<T> {
    private var entityClass: KClass<T> = this::class.findAnnotation<RestServiceFor>()?.let { it.entity as KClass<T> }
        ?: throw IllegalStateException("No valid annotation found on class ${this::class.simpleName}")

    @Autowired
    private lateinit var adapter: PersistencePort<T>

    @Autowired
    private lateinit var retriever: Retriever

    @Autowired
    private lateinit var redisService: RedisService

    private val log = KotlinLogging.logger {}


    override fun getSingle(id: UUID): T = adapter.getById(id)

    override fun getMultiple(ids: List<UUID>, page: Int, size: Int): Page<T> = adapter.getAllByIds(ids, page, size)

    override fun getAllPaged(page: Int, size: Int): Page<T> = adapter.getAllPaged(page, size)

    override fun search(request: SearchRequest, page: Int, size: Int): Page<T> {
        val startTime = System.currentTimeMillis()
        return try {
            val cachedResult = redisService.getCachedSearchResultsOrThrow(request, entityClass.simpleName!!)
            log.info("Search cached. Returning cached result.")
            val paginated = getMultiple(cachedResult, page, size)
            val endTime = System.currentTimeMillis()
            logResult(endTime, startTime, paginated)
            paginated
        } catch (e: NotCachedException) {
            log.info("Search not cached. Executing search.")
            val paginated = retriever.executeSearch(request, entityClass, page, size)
            redisService.cacheSearchResult(entityClass.simpleName!!, request, paginated.content)
            val endTime = System.currentTimeMillis()
            logResult(endTime, startTime, paginated)
            paginated
        }
    }

    private fun logResult(
        endTime: Long,
        startTime: Long,
        paginated: Page<T>
    ) {
        log.info(
            "Search completed in ${endTime - startTime}ms. Retrieved ${paginated.content.size}. Entity: ${entityClass.simpleName}."
        )
    }

}

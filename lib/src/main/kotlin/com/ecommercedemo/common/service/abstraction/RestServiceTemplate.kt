package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.application.exception.FailedToCreateException
import com.ecommercedemo.common.application.exception.FailedToDeleteException
import com.ecommercedemo.common.application.exception.FailedToUpdateException
import com.ecommercedemo.common.application.kafka.EntityEventProducer
import com.ecommercedemo.common.application.kafka.EntityEventType
import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.controller.abstraction.util.SearchParam
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.RestServiceFor
import com.ecommercedemo.common.service.concretion.EntityChangeTracker
import com.ecommercedemo.common.service.concretion.ServiceUtility
import com.ecommercedemo.common.service.concretion.TypeReAttacher
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Suppress("UNCHECKED_CAST")
abstract class RestServiceTemplate<T : BaseEntity>() : IRestService<T> {
    private var entityClass: KClass<T> = this::class.findAnnotation<RestServiceFor>()?.let { it.entity as KClass<T> }
        ?: throw IllegalStateException("No valid annotation found on class ${this::class.simpleName}")

    @Autowired
    private lateinit var adapter: IEntityPersistenceAdapter<T>

    @Autowired
    private lateinit var entityChangeTracker: EntityChangeTracker<T>

    @Autowired
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var eventProducer: EntityEventProducer

    @Autowired
    private lateinit var retriever: Retriever

    @Autowired
    private lateinit var serviceUtility: ServiceUtility<T>

    @Autowired
    private lateinit var typeReAttacher: TypeReAttacher

    @Autowired
    private lateinit var redisService: RedisService

    private val log = KotlinLogging.logger {}

    @Transactional
    override fun create(request: CreateRequest): T? {
        try {
            val typedRequestProperties = typeReAttacher.reAttachType(request.properties, entityClass)
            val newInstance = serviceUtility.createNewInstance(entityClass, typedRequestProperties)
            return saveAndEmitEvent(null, newInstance, EntityEventType.CREATE)
        } catch (e: Exception) {
            log.warn { "Failed to create. Cause: ${e.message}" }
            log.debug { "${e.stackTrace}" }
            throw FailedToCreateException("Failed to create", e)
        }

    }

    @Transactional
    override fun update(request: UpdateRequest): T {
        try {
            val original = getSingle(request.id)
            entityManager.detach(original)
            val typedRequestProperties = typeReAttacher.reAttachType(request.properties, entityClass)
            if (original::class != entityClass) {
                throw IllegalArgumentException(
                    "Entity type mismatch. Expected ${entityClass.simpleName} but found ${original::class.simpleName}."
                )
            }
            val updated = serviceUtility.updateExistingEntity(typedRequestProperties, original.copy() as T)

            return saveAndEmitEvent(original, updated, EntityEventType.UPDATE)
        } catch (e: Exception) {
            log.warn { "Failed to update. Cause: ${e.message}" }
            log.debug { "${e.stackTrace}" }
            throw FailedToUpdateException("Failed to update", e)
        }

    }

    //Todo: Consider returning the status in the controller, does not belong here
    @Transactional
    override fun delete(id: UUID): HttpStatus {
        return try {
            val entity = getSingle(id)
            adapter.delete(entity.id)
            eventProducer.emit(
                entity.javaClass.simpleName,
                id,
                EntityEventType.DELETE,
                mutableMapOf()
            )
            HttpStatus.OK
        } catch (e: Exception) {
            log.info { "Failed to delete. Cause: ${e.message}" }
            log.debug { "${e.stackTrace}" }
            throw FailedToDeleteException("Failed to delete", e)
        }
    }

    override fun getSingle(id: UUID): T = adapter.getById(id)

    //Todo: Consider optional pagination
    override fun getMultiple(ids: List<UUID>): List<T> = adapter.getAllByIds(ids)

    override fun getAllPaged(page: Int, size: Int): Page<T> = adapter.getAllPaged(page, size)

    override fun search(request: SearchRequest): List<T> {
        val startTime = System.currentTimeMillis()

        val cachedSearchResultsOrNullList = redisService.getCachedSearchResultsOrNullList(request, entityClass.simpleName!!)
        println("cachedSearchResultsOrNullList: $cachedSearchResultsOrNullList")

        val result: List<T>
        val cacheStatus: String

        when {
            cachedSearchResultsOrNullList.all { it != null } -> {
                result = getMultiple(redisService.combineCachedIds(cachedSearchResultsOrNullList))
                cacheStatus = "FULLY_CACHED"
            }

            cachedSearchResultsOrNullList.all { it == null } -> {
                result = computeWholeSearch(request)
                cacheStatus = "UNCACHED"
            }

            else -> {
                result = computePartialSearch(cachedSearchResultsOrNullList, request)
                cacheStatus = "PARTIALLY_CACHED"
            }
        }

        if (cachedSearchResultsOrNullList.any { it == null }) {
            redisService.overwriteSearchResults(
                entityName = entityClass.simpleName!!,
                searchRequest = request,
                resultIds = result.map { it.id }
            )
        }

        val endTime = System.currentTimeMillis()
        log.info(
            "Search completed in ${endTime - startTime}ms. Cache status: $cacheStatus. Entity: ${entityClass.simpleName}."
        )

        return result
    }

    private fun computePartialSearch(
        cachedSearchKeysList: List<Pair<SearchParam, List<UUID>>?>,
        request: SearchRequest
    ): List<T> = getMultiple(redisService.combineCachedIds(cachedSearchKeysList)) + retriever.executeSearch(
        SearchRequest(params = request.params.filterNot { param ->
            cachedSearchKeysList.any { it?.first == param }
        }), entityClass
    )

    private fun computeWholeSearch(request: SearchRequest): List<T> = retriever.executeSearch(request, entityClass)

    private fun saveAndEmitEvent(original: T?, updated: T, eventType: EntityEventType): T {
        val savedEntity = adapter.save(updated)
        val changes = original?.let { entityChangeTracker.getChangedProperties(it, savedEntity) }
            ?: entityChangeTracker.getChangedProperties(null, savedEntity)
        eventProducer.emit(entityClass.java.simpleName, savedEntity.id, eventType, changes)
        return savedEntity
    }


}

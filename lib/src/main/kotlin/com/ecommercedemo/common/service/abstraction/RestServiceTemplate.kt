package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.cache.CachingUtility
import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.application.exception.FailedToCreateException
import com.ecommercedemo.common.application.exception.FailedToDeleteException
import com.ecommercedemo.common.application.exception.FailedToUpdateException
import com.ecommercedemo.common.application.kafka.EntityEventProducer
import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.controller.abstraction.util.SearchParam
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.RestServiceFor
import com.ecommercedemo.common.service.concretion.EntityChangeTracker
import com.ecommercedemo.common.service.concretion.ReflectionService
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
    private lateinit var cachingUtility: CachingUtility

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

    @Autowired
    private lateinit var reflectionService: ReflectionService

    private val log = KotlinLogging.logger {}

    @Transactional
    override fun create(request: CreateRequest): T? {
        try {
            val typedRequestProperties = typeReAttacher.reAttachType(
                data = request.properties,
                entityClass = entityClass
            )

            val newInstance = serviceUtility.createNewInstance(
                instanceClass = entityClass,
                data = typedRequestProperties
            )

            val result = saveAndEmitEvent(
                original = null,
                updated = newInstance,
                eventType = ModificationType.CREATE
            )

            cachingUtility.invalidateSearchCaches(
                entityName = entityClass.simpleName!!,
                id = result.id,
                fields = request.properties.keys,
                modificationType = ModificationType.CREATE
            )

            return result
        } catch (e: Exception) {
            log.warn { "Failed to create. Cause: ${e.message}" }
            log.debug { "${e.stackTrace}" }
            //Todo: Annotate Exception with HttpStatus
            throw FailedToCreateException("Failed to create", e)
        }
    }

    @Transactional
    override fun update(request: UpdateRequest): T {
        try {
            val original = getSingle(request.id)
            entityManager.detach(original)
            val typedRequestProperties = typeReAttacher.reAttachType(
                data = request.properties,
                entityClass = entityClass
            )
            if (original::class != entityClass) {
                throw IllegalArgumentException(
                    "Entity type mismatch. Expected ${entityClass.simpleName} but found ${original::class.simpleName}."
                )
            }
            val updated = serviceUtility.updateExistingEntity(
                data = typedRequestProperties,
                entity = reflectionService.copy(original) as T
            )

            val result = saveAndEmitEvent(
                original = original,
                updated = updated,
                eventType = ModificationType.UPDATE
            )

            cachingUtility.invalidateSearchCaches(
                entityName = entityClass.simpleName!!,
                id = result.id,
                fields = request.properties.keys,
                modificationType = ModificationType.UPDATE
            )

            return result
        } catch (e: Exception) {
            log.warn { "Failed to update. Cause: ${e.message}" }
            log.debug { "${e.stackTrace}" }
            throw FailedToUpdateException("Failed to update", e)
        }

    }

    //Todo: Consider returning the status in the controller, does not belong here
    @Transactional
    override fun delete(id: UUID): HttpStatus {
        try {
            val entity = getSingle(id)
            adapter.delete(entity.id)
            eventProducer.emit(
                entityClassName = entity.javaClass.simpleName,
                id = id,
                modificationType = ModificationType.DELETE,
                properties = mutableMapOf()
            )
            cachingUtility.invalidateSearchCaches(
                entityName = entityClass.simpleName!!,
                id = id,
                fields = setOf(),
                modificationType = ModificationType.DELETE
            )
            return HttpStatus.OK
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

        val cachedSearchResultsOrNullList =
            redisService.getCachedSearchResultsOrNullList(request, entityClass.simpleName!!)

        val result: List<T>
        val cacheStatus: String

        when {
            cachedSearchResultsOrNullList.all { it != null } -> {
                result = getMultiple(cachingUtility.resultIntersection(cachedSearchResultsOrNullList))
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

        val endTime = System.currentTimeMillis()
        log.info(
            "Search completed in ${endTime - startTime}ms. Found ${result.size}. Cache status: $cacheStatus. Entity: ${entityClass.simpleName}."
        )

        return result
    }

    private fun computePartialSearch(
        cachedSearchResultsOrNullList: List<Pair<SearchParam, List<UUID>>?>,
        request: SearchRequest
    ): List<T> {
        val intersectionOfCachedIds = cachingUtility.resultIntersection(cachedSearchResultsOrNullList)
        val cacheBasedRetrieval = getMultiple(intersectionOfCachedIds)
        val uncachedParams = request.params.filterNot { param ->
            cachedSearchResultsOrNullList.any { it?.first == param }
        }
        val computedRetrieval = retriever.executeSearch(
            SearchRequest(params = uncachedParams), entityClass
        )
        return computedRetrieval.intersect(cacheBasedRetrieval.toSet()).toList()
    }

    //Todo: below works, but needs two calls to the database and two calls to the cache
//    private fun computePartialSearch(
//        cachedSearchKeysList: List<Pair<SearchParam, List<UUID>>?>,
//        request: SearchRequest
//    ): List<T> {
//        val uncachedParams = request.params.filterNot { param ->
//            cachedSearchKeysList.any { it?.first == param }
//        }
//        retriever.executeSearch(
//            SearchRequest(params = uncachedParams), entityClass
//        )
//        val updatedCachedSearchResultsOrNullList =
//            redisService.getCachedSearchResultsOrNullList(request, entityClass.simpleName!!)
//        return getMultiple(redisService.resultIntersection(updatedCachedSearchResultsOrNullList))
//    }

    private fun computeWholeSearch(request: SearchRequest): List<T> = retriever.executeSearch(request, entityClass)

    private fun saveAndEmitEvent(original: T?, updated: T, eventType: ModificationType): T {
        val savedEntity = adapter.save(updated)
        val changes = original?.let { entityChangeTracker.getChangedProperties(it, savedEntity) }
            ?: entityChangeTracker.getChangedProperties(null, savedEntity)
        eventProducer.emit(entityClass.java.simpleName, savedEntity.id, eventType, changes)
        return savedEntity
    }


}

package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.cache.CachingUtility
import com.ecommercedemo.common.application.cache.RedisService
import com.ecommercedemo.common.application.exception.FailedToCreateException
import com.ecommercedemo.common.application.exception.FailedToDeleteException
import com.ecommercedemo.common.application.exception.FailedToUpdateException
import com.ecommercedemo.common.application.exception.NotCachedException
import com.ecommercedemo.common.application.kafka.EntityEventProducer
import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.PersistencePort
import com.ecommercedemo.common.service.annotation.RestServiceFor
import com.ecommercedemo.common.service.concretion.EntityChangeTracker
import com.ecommercedemo.common.service.concretion.ReflectionService
import com.ecommercedemo.common.service.concretion.ServiceUtility
import com.ecommercedemo.common.service.concretion.TypeReAttacher
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Suppress("UNCHECKED_CAST")
@ConditionalOnClass(name = ["org.springframework.data.jpa.repository.JpaRepository"])
abstract class RestServiceTemplate<T : BaseEntity> : IRestService<T> {
    private var entityClass: KClass<T> = this::class.findAnnotation<RestServiceFor>()?.let { it.entity as KClass<T> }
        ?: throw IllegalStateException("No valid annotation found on class ${this::class.simpleName}")

    @Autowired
    private lateinit var adapter: PersistencePort<T>

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
        log.info { "Creating new entity of type ${entityClass.simpleName}" }
        try {
            println("Attempting to reattach type for entity class: ${entityClass.simpleName}")
            val typedRequestProperties = typeReAttacher.reAttachType(
                data = request.properties,
                entityClassName = entityClass.simpleName!!
            )

            println("Attempting to create new instance of type: ${entityClass.simpleName}")
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
                entityClassName = entityClass.simpleName!!
            )
            if (original::class != entityClass) {
                throw IllegalArgumentException(
                    "Entity type mismatch. Expected ${entityClass.simpleName} but found ${original::class.simpleName}."
                )
            }
            val updated = serviceUtility.updateExistingEntity(
                data = typedRequestProperties,
                entity = original.copy() as T
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

    private fun saveAndEmitEvent(original: T?, updated: T, eventType: ModificationType): T {
        val savedEntity = adapter.save(updated)
        val changes = original?.let { entityChangeTracker.getChangedProperties(it, savedEntity) }
            ?: entityChangeTracker.getChangedProperties(null, savedEntity)
        eventProducer.emit(entityClass.java.simpleName, savedEntity.id, eventType, changes)
        return savedEntity
    }

}

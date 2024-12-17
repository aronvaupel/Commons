package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.exception.FailedToCreateException
import com.ecommercedemo.common.application.exception.FailedToDeleteException
import com.ecommercedemo.common.application.exception.FailedToUpdateException
import com.ecommercedemo.common.application.kafka.EntityEventProducer
import com.ecommercedemo.common.application.kafka.EntityEventType
import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
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

@Suppress("UNCHECKED_CAST")
abstract class RestServiceTemplate<T : BaseEntity>: IRestService<T> {
    @Autowired
    private lateinit var adapter: IEntityPersistenceAdapter<T>
    private lateinit var entityClass: KClass<T>
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

            return saveAndEmitEvent( original, updated, EntityEventType.UPDATE,)
        } catch (e: Exception) {
            log.warn { "Failed to update. Cause: ${e.message}" }
            log.debug { "${e.stackTrace}" }
            throw FailedToUpdateException("Failed to update", e)
        }

    }

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

    override fun getMultiple(ids: List<UUID>): List<T> = adapter.getAllByIds(ids)

    override fun getAllPaged(page: Int, size: Int): Page<T> = adapter.getAllPaged(page, size)

    override fun search(request: SearchRequest): List<T> = retriever.executeSearch(request, entityClass)

    private fun saveAndEmitEvent(original: T?, updated: T, eventType: EntityEventType, ): T {
        val savedEntity = adapter.save(updated)
        val changes = original?.let { entityChangeTracker.getChangedProperties(it, savedEntity) }
            ?: entityChangeTracker.getChangedProperties(null, savedEntity)
        eventProducer.emit(entityClass.java.simpleName, savedEntity.id, eventType, changes)

        return savedEntity
    }

}

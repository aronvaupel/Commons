package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.EntityChangeTracker
import com.ecommercedemo.common.application.event.EntityEventProducer
import com.ecommercedemo.common.application.event.EntityEventType
import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.concretion.ServiceUtility
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST")
abstract class RestServiceTemplate<T : BaseEntity>(
    private val adapter: IEntityPersistenceAdapter<T>,
    private val entityClass: KClass<T>,
    private val eventProducer: EntityEventProducer,
    private val retriever: Retriever,
    private val serviceUtility: ServiceUtility
) : IRestService<T> {

    @Transactional
    override fun create(request: CreateRequest<T>): T {
        val newInstance = serviceUtility.createNewInstance(request.data::class) { name ->
            request.data::class.memberProperties.firstOrNull { it.name == name }?.getter?.call(request.data)
        }

        return saveAndEmitEvent(null, newInstance, EntityEventType.CREATE)
    }

    @Transactional
    override fun update(request: UpdateRequest): T {
        println("Attempting to update entity with ID ${request.id}")
        val original = getSingle(request.id)
        println("Original entity: $original")
        if (original::class != entityClass) {
            throw IllegalArgumentException(
                "Entity type mismatch. Expected ${entityClass.simpleName} but found ${original::class.simpleName}."
            )
        }

        val updated = serviceUtility.updateExistingInstance(original.copy() as T, request.properties)
        println("Updated entity: $updated")

        return saveAndEmitEvent( original, updated, EntityEventType.UPDATE,)
    }

    @Transactional
    override fun delete(id: UUID): HttpStatus {
        return try {
            val entity = getSingle(id)
            adapter.delete(entity.id)
            eventProducer.emit(
                entity::class.java,
                id,
                EntityEventType.DELETE,
                mutableMapOf()
            )
            HttpStatus.OK
        } catch (e: Exception) {
            println("Error deleting entity with ID $id: ${e.message}")
            e.printStackTrace()
            HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    override fun getSingle(id: UUID): T = adapter.getById(id)

    override fun getMultiple(ids: List<UUID>): List<T> = adapter.getAllByIds(ids)

    override fun search(request: SearchRequest): List<T> = retriever.executeSearch(request, entityClass)

    private fun saveAndEmitEvent(original: T?, updated: T, eventType: EntityEventType, ): T {
        val savedEntity = adapter.save(updated)
        println("SaveAndEmitEvent: Saved entity: $savedEntity")
        val tracker = EntityChangeTracker<T>(serviceUtility)
        val changes = original?.let { tracker.getChangedProperties(it, savedEntity) }
            ?: tracker.getChangedProperties(null, savedEntity)
        println("SaveAndEmitEvent: Changes: $changes")
        eventProducer.emit(entityClass.java, savedEntity.id, eventType, changes)

        return savedEntity
    }

}

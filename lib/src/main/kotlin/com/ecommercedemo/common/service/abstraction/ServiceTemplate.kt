package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.event.EntityEvent
import com.ecommercedemo.common.application.event.EntityEventProducer
import com.ecommercedemo.common.application.event.EntityEventType
import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.utility.ServiceUtility
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Suppress("UNCHECKED_CAST")
abstract class ServiceTemplate<T : BaseEntity>(
    private val adapter: IEntityPersistenceAdapter<T>,
    private val entityClass: KClass<T>,
    private val eventProducer: EntityEventProducer,
    private val objectMapper: ObjectMapper,
    private val pseudoPropertyRepository: EntityRepository<out BasePseudoProperty, UUID>,
    private val retriever: Retriever,
    private val utility: ServiceUtility<T>
) : IService<T> {

    @Transactional
    override fun create(request: CreateRequest<T>): T {
        val newInstance = utility.constructEntity(request.data::class.memberProperties.associateBy { it.name }) { name ->
            request.data::class.memberProperties.firstOrNull { it.name == name }?.getter?.call(request.data)
        }

        utility.handlePseudoPropertiesIfPresent(newInstance, request.data)

        return utility.saveAndEmitEvent(newInstance, EntityEventType.CREATE, null)
    }

    @Transactional
    override fun createByEvent(event: EntityEvent<T>) {
        val newInstance = utility.constructEntity(event.properties) { name ->
            event.properties[name]
        }

        utility.handlePseudoPropertiesIfPresent(newInstance, event.properties)

        adapter.save(newInstance)
    }

    @Transactional
    override fun update(request: UpdateRequest): T {
        val originalEntity = getSingle(request.id)

        if (originalEntity != entityClass) {
            throw IllegalArgumentException(
                "Entity type mismatch. Expected ${entityClass.simpleName} but found ${originalEntity::class.java.simpleName}."
            )
        }

        val updatedEntity = utility.mapPropertiesToEntity(originalEntity.copy() as T, request.properties)

        utility.handlePseudoPropertiesIfPresent(updatedEntity, request.properties)

        return utility.saveAndEmitEvent(updatedEntity, EntityEventType.UPDATE, originalEntity)
    }

    @Transactional
    override fun updateByEvent(event: EntityEvent<T>) {
        val originalEntity = getSingle(event.id)

        val updatedEntity = utility.mapPropertiesToEntity(originalEntity.copy() as T, event.properties)

        utility.handlePseudoPropertiesIfPresent(updatedEntity, event.properties)

        adapter.save(updatedEntity)
    }

    @Transactional
    override fun delete(id: UUID): HttpStatus {
        return try {
            val entity = utility.handleMissingEntity { getSingle(id) } ?: return HttpStatus.NOT_FOUND
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

    @Transactional
    override fun deleteByEvent(event: EntityEvent<T>) {
        try {
            val entity = utility.handleMissingEntity { getSingle(event.id) }
            if (entity != null) {
                adapter.delete(entity.id)
            }
        } catch (e: Exception) {
            println("Error deleting entity with ID ${event.id}: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun getSingle(id: UUID): T = adapter.getById(id)

    override fun getMultiple(ids: List<UUID>): List<T> = adapter.getAllByIds(ids)

    override fun search(request: SearchRequest): List<T> = retriever.executeSearch(request, entityClass)
}

package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.event.EntityEvent
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import jakarta.transaction.Transactional
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "unused")
abstract class EventServiceTemplate<T: BaseEntity, R: BaseEntity>(
    private val adapter: IEntityPersistenceAdapter<R>,
    private val serviceUtility: ServiceUtility,
    private val downstreamEntityClass: KClass<R>
): IEventService<T, R> {
    @Transactional
    override fun createByEvent(event: EntityEvent<T>) {
        val newInstance = serviceUtility.instantiateEntity(downstreamEntityClass) { name ->
            event.properties[name]
        }

        serviceUtility.handlePseudoPropertiesIfPresent(newInstance, event.properties)

        adapter.save(newInstance)
    }

    @Transactional
    override fun updateByEvent(event: EntityEvent<T>) {
        val originalEntity = adapter.getById(event.id)

        val updatedEntity = serviceUtility.applyPropertiesToExistingEntity(originalEntity.copy() as R, event.properties)

        serviceUtility.handlePseudoPropertiesIfPresent(updatedEntity, event.properties)

        adapter.save(updatedEntity)
    }

    @Transactional
    override fun deleteByEvent(event: EntityEvent<T>) {
        try {
            val entity =  adapter.getById(event.id)
            adapter.delete(entity.id)
        } catch (e: NoSuchElementException) {
            println("Error: Entity not found. Cannot proceed with the operation.")
            e.printStackTrace()
        }
    }
}
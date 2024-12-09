package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.event.EntityEvent
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.concretion.ServiceUtility
import jakarta.transaction.Transactional
import java.util.*
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "unused")
abstract class EventServiceTemplate<T : BaseEntity, R : BaseEntity>(
    private val adapter: IEntityPersistenceAdapter<R>,
    private val serviceUtility: ServiceUtility,
    private val downstreamEntityClass: KClass<R>
) : IEventService<T, R> {
    @Transactional
    override fun createByEvent(event: EntityEvent<T>) {
        val newInstance = serviceUtility.createNewInstance(downstreamEntityClass) { name ->
            event.properties[name]
        }

        adapter.save(newInstance.apply { id = event.properties[BaseEntity::id.name] as UUID})
    }

    @Transactional
    override fun updateByEvent(event: EntityEvent<T>) {
        val original = adapter.getById(event.id)

        val updated = serviceUtility.updateExistingInstance(original.copy() as R, event.properties)

        adapter.save(updated)
    }

    @Transactional
    override fun deleteByEvent(event: EntityEvent<T>) {
        try {
            val entity = adapter.getById(event.id)
            adapter.delete(entity.id)
        } catch (e: NoSuchElementException) {
            println("Error: Entity not found. Cannot proceed with the operation.")
            e.printStackTrace()
        }
    }
}
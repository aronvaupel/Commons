package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.concretion.ServiceUtility
import com.ecommercedemo.common.service.concretion.TypeReAttacher
import jakarta.transaction.Transactional
import java.util.*
import kotlin.reflect.KClass

@Suppress("unused", "UNCHECKED_CAST")
abstract class EventServiceTemplate<T : BaseEntity>(
    private val adapter: IEntityPersistenceAdapter<T>,
    private val downstreamEntityClass: KClass<T>,
    private val serviceUtility: ServiceUtility,
    private val typeReAttacher: TypeReAttacher,
) : IEventService<T> {
    @Transactional
    override fun createByEvent(event: EntityEvent) {
        val typedProperties =  typeReAttacher.reAttachType(event.properties, downstreamEntityClass)
        val newInstance = serviceUtility.createNewInstance(downstreamEntityClass, typedProperties).apply { id = event.properties[BaseEntity::id.name] as UUID}
        adapter.save(newInstance)
    }

    @Transactional
    override fun updateByEvent(event: EntityEvent) {
        val original = adapter.getById(event.id)

        val updated = serviceUtility.updateExistingEntity(event.properties, original.copy())

        adapter.save(updated as T)
    }

    @Transactional
    override fun deleteByEvent(event: EntityEvent) {
        try {
            val entity = adapter.getById(event.id)
            adapter.delete(entity.id)
        } catch (e: NoSuchElementException) {
            println("Error: Entity not found. Cannot proceed with the operation.")
            e.printStackTrace()
        }
    }
}
package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.exception.FailedToCreateByEventException
import com.ecommercedemo.common.application.exception.FailedToDeleteByEventException
import com.ecommercedemo.common.application.exception.FailedToUpdateByEventException
import com.ecommercedemo.common.application.kafka.EntityEvent
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.service.concretion.ServiceUtility
import com.ecommercedemo.common.service.concretion.TypeReAttacher
import jakarta.transaction.Transactional
import mu.KotlinLogging
import java.util.*
import kotlin.reflect.KClass

@Suppress("unused", "UNCHECKED_CAST")
abstract class EventServiceTemplate<T : BaseEntity>(
    private val adapter: IEntityPersistenceAdapter<T>,
    private val downstreamEntityClass: KClass<T>,
    private val serviceUtility: ServiceUtility<T>,
    private val typeReAttacher: TypeReAttacher,
) : IEventService<T> {
    val log = KotlinLogging.logger {}

    @Transactional
    override fun createByEvent(event: EntityEvent) {
        try {
            val typedProperties =  typeReAttacher.reAttachType(event.properties, downstreamEntityClass)
            val newInstance = serviceUtility.createNewInstance(downstreamEntityClass, typedProperties).apply { id = event.properties[BaseEntity::id.name] as UUID}
            adapter.save(newInstance)
        } catch (e: Exception) {
            log.warn { "${e.message}" }
            log.debug { "${e.stackTrace}" }
            throw FailedToCreateByEventException("Failed to create by event", e)
        }

    }

    @Transactional
    override fun updateByEvent(event: EntityEvent) {
        try {
            val original = adapter.getByIdWithLock(event.id)
            val updated = serviceUtility.updateExistingEntity(event.properties, original.copy() as T)
            adapter.save(updated)
        } catch(e: Exception)  {
            log.warn { "${e.message}" }
            log.debug { "${e.stackTrace}" }
            throw FailedToUpdateByEventException("Failed to update by event", e)
        }

    }

    @Transactional
    override fun deleteByEvent(event: EntityEvent) {
        try {
            val entity = adapter.getByIdWithLock(event.id)
            adapter.delete(entity.id)
        } catch (e: Exception) {
            log.warn { "${e.message}" }
            log.debug { "${e.stackTrace}" }
            throw FailedToDeleteByEventException("Failed to delete by event", e)
        }
    }
}
package com.ecommercedemo.common.service.concretion._pseudoProperty

import com.ecommercedemo.common.application.kafka.EntityEventProducer
import com.ecommercedemo.common.application.validation.modification.ModificationType
import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.persistence.abstraction.PersistencePort
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.util.concurrent.RateLimiter
import jakarta.transaction.Transactional
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.*

@Service
@Suppress("UNCHECKED_CAST", "unused", "ClassName")
//Todo: rate limit for other methods as well
open class _PseudoPropertyApplier(
    private val beanFactory: BeanFactory,
    private val eventProducer: EntityEventProducer,
) {
    private val objectMapper = jacksonObjectMapper()
    private val rateLimiter = RateLimiter.create(100.0)

    @Async
    @Transactional
    open fun addPseudoPropertyToAllEntitiesOfType(
        entityClass: Class<out AugmentableBaseEntity>,
        key: String
    ) {
        val adapter = getAdapter(entityClass)
        val pageSize = 100
        var page = 0

        do {
            val pagedEntities = adapter.getAllPaged(page, pageSize)

            if (pagedEntities.hasContent()) {
                val entities = pagedEntities.content

                entities.forEach { entity ->
                    entity.getPseudoProperty(key)?.let {
                        throw IllegalArgumentException(
                            "Entity ${entity.id} already contains the key '$key'. Cannot override."
                        )
                    } ?:entity.addPseudoProperty(key, null)
                }

                adapter.saveAll(entities)

                entities.forEach { entity ->
                    rateLimiter.acquire()
                    eventProducer.emit(
                        entity.javaClass.simpleName,
                        entity.id,
                        ModificationType.CREATE,
                        getChanges(entity)
                    )
                }
            }
            page++
        } while (pagedEntities.hasNext())
    }


    @Transactional
    @Async
    open fun renamePseudoPropertyForAllEntitiesOfType(
        entityClass: Class<out AugmentableBaseEntity>,
        oldKey: String,
        newKey: String
    ) {
        val adapter = getAdapter(entityClass)
        val pageSize = 100
        var page = 0

        do {
            val pagedEntities = adapter.getAllPaged(page, pageSize)
            if (pagedEntities.hasContent()) {
                val entities = pagedEntities.content

                entities.forEach { entity ->
                        entity.renamePseudoProperty(oldKey, newKey)
                }

                adapter.saveAll(entities)
                entities.forEach { entity ->
                    eventProducer.emit(
                        entity.javaClass.simpleName,
                        entity.id,
                        ModificationType.UPDATE,
                        getChanges(entity)
                    )
                }
            }

            page++
        } while (pagedEntities.hasNext())
    }

    @Transactional
    open fun deletePseudoPropertyForAllEntitiesOfType(
        entityClass: Class<out AugmentableBaseEntity>,
        key: String
    ) {
        val adapter = getAdapter(entityClass)
        val pageSize = 1000
        var page = 0

        do {
            val pagedEntities = adapter.getAllPaged(page, pageSize)

            if (pagedEntities.hasContent()) {
                val entities = pagedEntities.content

                entities.forEach { entity ->
                    entity.getPseudoProperty(key)?.let {
                        entity.removePseudoProperty(key)
                    }
                }

                adapter.saveAll(entities)
                entities.forEach { entity ->
                    eventProducer.emit(
                        entity.javaClass.simpleName,
                        entity.id,
                        ModificationType.DELETE,
                        getChanges(entity)
                    )
                }
            }

            page++
        } while (pagedEntities.hasNext())
    }

    private fun getAdapter(entityClass: Class<*>): PersistencePort<AugmentableBaseEntity> {
        val adapterName = "${entityClass.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }}PersistenceAdapter"
        val adapter = try {
            beanFactory.getBean(adapterName)
        } catch (e: NoSuchBeanDefinitionException) {
            throw IllegalArgumentException("Adapter for entity class '${entityClass.simpleName}' not found.", e)
        }
        return adapter as? PersistencePort<AugmentableBaseEntity>
            ?: throw IllegalArgumentException("Adapter for '${entityClass.simpleName}' is not a IEntityPersistenceAdapter.")
    }

    private fun getChanges(entity: AugmentableBaseEntity): MutableMap<String, Any?> =
        mutableMapOf(entity::pseudoProperties.name to entity.pseudoProperties)
}

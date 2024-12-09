package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.event.EntityEventProducer
import com.ecommercedemo.common.application.event.EntityEventType
import com.ecommercedemo.common.model.abstraction.ExpandableBaseEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.EntityManagerFactory
import jakarta.transaction.Transactional
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
@Suppress("UNCHECKED_CAST", "unused")
open class PseudoPropertyApplier(
    private val beanFactory: BeanFactory,
    private val eventProducer: EntityEventProducer,
    private val entityManagerFactory: EntityManagerFactory,
) {
    private val objectMapper = jacksonObjectMapper()
    private fun getEntityRepository(entityClass: Class<*>): JpaRepository<ExpandableBaseEntity, UUID> {
        val repositoryName =
            "${entityClass.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }}Repository"
        val repository = try {
            beanFactory.getBean(repositoryName)
        } catch (e: NoSuchBeanDefinitionException) {
            throw IllegalArgumentException("Repository for entity class '${entityClass.simpleName}' not found.", e)
        }
        return repository as? JpaRepository<ExpandableBaseEntity, UUID>
            ?: throw IllegalArgumentException("Repository for '${entityClass.simpleName}' is not a JpaRepository.")
    }

    @Transactional
    open fun deletePseudoPropertyForAllEntitiesOfType(entityClass: Class<out ExpandableBaseEntity>, key: String) {
        val repository = getEntityRepository(entityClass)
        repository.findAll().forEach { entity ->
            val deserializedPseudoProperties = objectMapper.readValue(
                objectMapper.writeValueAsString(entity.pseudoProperties),
                object : TypeReference<Map<String, Any?>>() {}).toMutableMap()
            if (!deserializedPseudoProperties.containsKey(key)) {
                throw IllegalArgumentException(
                    "Entity ${entity.id} of type '${entityClass.simpleName}' does not contain the key '$key'. Cannot delete."
                )
            }
            deserializedPseudoProperties.remove(key)
            entity.pseudoProperties = objectMapper.writeValueAsString(deserializedPseudoProperties)
            repository.save(entity)
            eventProducer.emit(
                entity.javaClass,
                entity.id,
                EntityEventType.DELETE,
                getChanges(entity)
            )
        }
    }

    @Transactional
    open fun addPseudoPropertyToAllEntitiesOfType(
        entityClass: Class<out ExpandableBaseEntity>,
        key: String,
    ) {
        val repository = getEntityRepository(entityClass)

        repository.findAll().forEach { entity ->
            val deserializedPseudoProperties = if (entity.pseudoProperties.isNotEmpty()) {
                objectMapper.readValue<Map<String, Any?>>(entity.pseudoProperties).toMutableMap()
            } else {
                mutableMapOf()
            }

            if (deserializedPseudoProperties.containsKey(key)) {
                throw IllegalArgumentException(
                    "Entity ${entity.id} of type '${entityClass.simpleName}' already contains the key '$key'. Cannot override."
                )
            }

            deserializedPseudoProperties[key] = null

            entity.pseudoProperties = objectMapper.writeValueAsString(deserializedPseudoProperties)

            repository.save(entity)

            eventProducer.emit(
                entity.javaClass,
                entity.id,
                EntityEventType.CREATE,
                getChanges(entity)
            )
        }
    }

    @Transactional
    open fun renamePseudoPropertyForAllEntitiesOfType(
        entityClass: Class<out ExpandableBaseEntity>,
        oldKey: String,
        newKey: String
    ) {
        val repository = getEntityRepository(entityClass)
        repository.findAll().forEach { entity ->
            val deserializedPseudoProperties = objectMapper.readValue(
                objectMapper.writeValueAsString(entity.pseudoProperties),
                object : TypeReference<Map<String, Any?>>() {}).toMutableMap()
            if (!deserializedPseudoProperties.containsKey(oldKey))
                throw IllegalArgumentException(
                    "Entity ${entity.id} of type '${entityClass.simpleName}' does not contain the key '$oldKey'. Cannot rename."
                )
            if (deserializedPseudoProperties.containsKey(newKey))
                throw IllegalArgumentException(
                    "Entity ${entity.id} of type '${entityClass.simpleName}' already contains the key '$newKey'. Cannot rename."
                )

            deserializedPseudoProperties[newKey] = deserializedPseudoProperties.remove(oldKey)
            entity.pseudoProperties = objectMapper.writeValueAsString(deserializedPseudoProperties)
            repository.save(entity)
            eventProducer.emit(
                entity.javaClass,
                entity.id,
                EntityEventType.UPDATE,
                getChanges(entity)
            )
        }
    }

    private fun getChanges(entity: ExpandableBaseEntity): MutableMap<String, Any?> =
        mutableMapOf(entity::pseudoProperties.name to entity.pseudoProperties)
}

package com.ecommercedemo.common.service.concretion._pseudoProperty

import com.ecommercedemo.common.application.kafka.EntityEventProducer
import com.ecommercedemo.common.application.kafka.EntityEventType
import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.EntityManagerFactory
import jakarta.transaction.Transactional
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
@Suppress("UNCHECKED_CAST", "unused", "ClassName")
open class _PseudoPropertyApplier(
    private val beanFactory: BeanFactory,
    private val eventProducer: EntityEventProducer,
    private val entityManagerFactory: EntityManagerFactory,
) {
    private val objectMapper = jacksonObjectMapper()

    @Transactional
    open fun addPseudoPropertyToAllEntitiesOfType(
        entityClass: Class<out AugmentableBaseEntity>,
        key: String,
    ) {
        val repository = getEntityRepository(entityClass)
        repository.findAll().forEach { entity ->
            entity.getPseudoProperty(key)?.let {
                throw IllegalArgumentException(
                    "Entity ${entity.id} of type '${entityClass.simpleName}' already contains the key '$key'. Cannot override."
                )
            } ?: entity.setPseudoProperty(key, null)
            repository.save(entity)
            eventProducer.emit(
                entity.javaClass.simpleName,
                entity.id,
                EntityEventType.CREATE,
                getChanges(entity)
            )
        }
    }

    @Transactional
    open fun renamePseudoPropertyForAllEntitiesOfType(
        entityClass: Class<out AugmentableBaseEntity>,
        oldKey: String,
        newKey: String
    ) {
        val repository = getEntityRepository(entityClass)
        repository.findAll().forEach { entity ->
            entity.renamePseudoProperty(oldKey, newKey)
            repository.save(entity)
            eventProducer.emit(
                entity.javaClass.simpleName,
                entity.id,
                EntityEventType.UPDATE,
                getChanges(entity)
            )
        }
    }

    @Transactional
    open fun deletePseudoPropertyForAllEntitiesOfType(entityClass: Class<out AugmentableBaseEntity>, key: String) {
        val repository = getEntityRepository(entityClass)
        repository.findAll().forEach { entity ->
            entity.removePseudoProperty(key)
            repository.save(entity)
            eventProducer.emit(
                entity.javaClass.simpleName,
                entity.id,
                EntityEventType.DELETE,
                getChanges(entity)
            )
        }
    }

    private fun getRepositoryName(entityClass: Class<*>): String {
        val repositoryName =
            "${entityClass.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }}Repository"
        return repositoryName
    }

    private fun getEntityRepository(entityClass: Class<*>): JpaRepository<AugmentableBaseEntity, UUID> {
        val repositoryName =
            getRepositoryName(entityClass)
        val repository = try {
            beanFactory.getBean(repositoryName)
        } catch (e: NoSuchBeanDefinitionException) {
            throw IllegalArgumentException("Repository for entity class '${entityClass.simpleName}' not found.", e)
        }
        return repository as? JpaRepository<AugmentableBaseEntity, UUID>
            ?: throw IllegalArgumentException("Repository for '${entityClass.simpleName}' is not a JpaRepository.")
    }

    private fun getChanges(entity: AugmentableBaseEntity): MutableMap<String, Any?> =
        mutableMapOf(entity::pseudoProperties.name to entity.pseudoProperties)
}

package com.ecommercedemo.common.service

import com.ecommercedemo.common.kafka.EntityEventProducer
import com.ecommercedemo.common.kafka.EntityEventType
import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.model.dto.PseudoPropertyDto
import com.ecommercedemo.common.persistence.IPseudoPropertyAdapter
import com.ecommercedemo.common.persistence.IPseudoPropertyManagement
import com.ecommercedemo.common.util.search.dto.SearchRequest
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.stereotype.Service
import java.util.*

@Service
class PseudoPropertyService(
    private val beanFactory: BeanFactory,
    private val pseudoPropertyAdapter: IPseudoPropertyAdapter,
    private val eventProducer: EntityEventProducer,
) {

    private fun getRepositoryForEntity(entityClass: String): IPseudoPropertyManagement {
        if (entityClass.startsWith("_")) {
            throw IllegalArgumentException(
                "Downstream entity '$entityClass' is not directly manageable. Use event handling for updates."
            )
        }

        val repository = try {
            beanFactory.getBean("${entityClass}Repository")
        } catch (e: NoSuchBeanDefinitionException) {
            throw IllegalArgumentException("Entity '$entityClass' is not managed by this service.")
        }

        return repository as? IPseudoPropertyManagement
            ?: throw IllegalArgumentException(
                "Repository for entity '$entityClass' does not support pseudo-property management."
            )
    }

    fun addPseudoProperty(pseudoProperty: PseudoPropertyDto): PseudoProperty {
        val repository = getRepositoryForEntity(pseudoProperty.entityClassName)
        val result = pseudoPropertyAdapter.save(pseudoProperty)
        repository.updatePseudoPropertyForAllEntities(pseudoProperty.key, null)
        eventProducer.emit(PseudoProperty::class.java, result.id, EntityEventType.CREATE, mutableMapOf())
        return result
    }

    fun deletePseudoProperty(id: UUID) {
        val pseudoProperty = pseudoPropertyAdapter.getById(id)
        (getRepositoryForEntity(pseudoProperty.entityClassName))
            .deletePseudoPropertyForAllEntities(pseudoProperty.key)
        pseudoPropertyAdapter.delete(pseudoProperty.id)
        getRepositoryForEntity(pseudoProperty.entityClassName).deletePseudoPropertyForAllEntities(pseudoProperty.key)
        eventProducer.emit(PseudoProperty::class.java, pseudoProperty.id, EntityEventType.DELETE, mutableMapOf())
    }

    fun getById(id: UUID) = pseudoPropertyAdapter.getById(id)

    fun getAll(request: SearchRequest) = pseudoPropertyAdapter.getPseudoProperties(request)

    fun update(id: UUID, body: PseudoPropertyDto): PseudoProperty {
        val repository = getRepositoryForEntity(body.entityClassName)
        val old = pseudoPropertyAdapter.getById(id)
        val updatedPseudoProperty = old.copy(
            entityClassName = body.entityClassName.ifBlank { old.entityClassName },
            key = body.key.ifBlank { old.key },
            value = body.valueType
        )
        val changes = mapOf(
            PseudoProperty::entityClassName.name to updatedPseudoProperty.entityClassName.takeIf { it != old.entityClassName },
            PseudoProperty::key.name to updatedPseudoProperty.key.takeIf { it != old.key },
            PseudoProperty::valueType.name to updatedPseudoProperty.valueType.takeIf { it != old.valueType }
        ).filterValues { it != null }.toMutableMap()
        val result = pseudoPropertyAdapter.save(updatedPseudoProperty.toDto())
        repository.updatePseudoPropertyForAllEntities(old.key, updatedPseudoProperty.key)
        eventProducer.emit(PseudoProperty::class.java, result.id, EntityEventType.UPDATE, changes)
        return result
    }
}

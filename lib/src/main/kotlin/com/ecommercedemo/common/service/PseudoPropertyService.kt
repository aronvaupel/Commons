package com.ecommercedemo.common.service

import com.ecommercedemo.common.kafka.EntityEventProducer
import com.ecommercedemo.common.kafka.EntityEventType
import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.persistence.IPseudoPropertyAdapter
import com.ecommercedemo.common.persistence.IPseudoPropertyManagement
import com.ecommercedemo.common.util.search.dto.SearchRequest
import org.springframework.beans.factory.BeanFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class PseudoPropertyService(
    private val beanFactory: BeanFactory,
    private val pseudoPropertyAdapter: IPseudoPropertyAdapter,
    private val eventProducer: EntityEventProducer,
) {

    private fun getRepositoryForEntity(entityClass: String) =
        beanFactory.getBean("$entityClass${"Repository"}")

    fun addPseudoProperty(pseudoProperty: PseudoProperty): PseudoProperty {
        val result = pseudoPropertyAdapter.save(pseudoProperty)
        (getRepositoryForEntity(pseudoProperty.entityClassName) as? IPseudoPropertyManagement)
            ?.updatePseudoPropertyForAllEntities(pseudoProperty.key, null)
        eventProducer.emit(PseudoProperty::class.java, result.id, EntityEventType.CREATE, mutableMapOf())
        return result
    }

    fun deletePseudoProperty(id: UUID) {
        val pseudoProperty = pseudoPropertyAdapter.getById(id)
        (getRepositoryForEntity(pseudoProperty.entityClassName) as? IPseudoPropertyManagement)
            ?.deletePseudoPropertyForAllEntities(pseudoProperty.key)
        pseudoPropertyAdapter.delete(pseudoProperty.id)
        eventProducer.emit(PseudoProperty::class.java, pseudoProperty.id, EntityEventType.DELETE, mutableMapOf())
    }

    fun getById(id: UUID) = pseudoPropertyAdapter.getById(id)

    fun getAll(request: SearchRequest) = pseudoPropertyAdapter.getAll(request)

    fun update(id: UUID, new: PseudoProperty): PseudoProperty {
        val old = pseudoPropertyAdapter.getById(id)
        val updatedPseudoProperty = old.copy(
            entityClassName = new.entityClassName.ifBlank { old.entityClassName },
            key = new.key.ifBlank { old.key },
            value = new.valueType ?: old.valueType
        )
        val changes = mapOf(
            PseudoProperty::entityClassName.name to updatedPseudoProperty.entityClassName.takeIf { it != old.entityClassName },
            PseudoProperty::key.name to updatedPseudoProperty.key.takeIf { it != old.key },
            PseudoProperty::valueType.name to updatedPseudoProperty.valueType.takeIf { it != old.valueType }
        ).filterValues { it != null }.toMutableMap()
        val result = pseudoPropertyAdapter.save(updatedPseudoProperty)
        eventProducer.emit(PseudoProperty::class.java, result.id, EntityEventType.UPDATE, changes)
        return result
    }
}

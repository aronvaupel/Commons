package com.ecommercedemo.common.service

import com.ecommercedemo.common.application.event.EntityEventProducer
import com.ecommercedemo.common.application.event.EntityEventType
import com.ecommercedemo.common.application.search.dto.SearchRequest
import com.ecommercedemo.common.model.ExtendableBaseEntity
import com.ecommercedemo.common.model.PseudoProperty
import com.ecommercedemo.common.model.dto.PseudoPropertyDto
import com.ecommercedemo.common.persistence.IPseudoPropertyAdapter
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.*

@Transactional
@Service
@Suppress("UNCHECKED_CAST")
open class PseudoPropertyService(
    private val pseudoPropertyAdapter: IPseudoPropertyAdapter,
    private val pseudoPropertyApplier: PseudoPropertyApplier,
    private val eventProducer: EntityEventProducer,
) {

    open fun addPseudoProperty(dto: PseudoPropertyDto): PseudoProperty {
        val result = pseudoPropertyAdapter.saveAsJsonb(dto)
        println("Saved pseudo property: $result")
        pseudoPropertyApplier.addPseudoPropertyToAllEntitiesOfType(
            Class.forName(dto.entityClassName) as Class<out ExtendableBaseEntity>,
            dto.key,
            dto.typeDescriptor
        )
        eventProducer.emit(PseudoProperty::class.java, result.id, EntityEventType.CREATE, mutableMapOf())
        return result
    }

    fun deletePseudoProperty(id: UUID) {
        val pseudoProperty = pseudoPropertyAdapter.getById(id)
        pseudoPropertyApplier.deletePseudoPropertyForAllEntitiesOfType(
            Class.forName(pseudoProperty.entitySimpleName) as Class<out ExtendableBaseEntity>,
            pseudoProperty.key
        )
        pseudoPropertyAdapter.delete(pseudoProperty.id)
        eventProducer.emit(PseudoProperty::class.java, pseudoProperty.id, EntityEventType.DELETE, mutableMapOf())
    }

    fun renamePseudoProperty(id: UUID, newKey: String): PseudoProperty {
        val pseudoProperty = pseudoPropertyAdapter.getById(id)
        val result = pseudoPropertyAdapter.saveAsJsonb(
            pseudoProperty.apply { key = newKey }.toDto()
        )
        pseudoPropertyApplier.renamePseudoPropertyForAllEntitiesOfType(
            Class.forName(pseudoProperty.entitySimpleName) as Class<out ExtendableBaseEntity>,
            pseudoProperty.key,
            newKey
        )
        eventProducer.emit(PseudoProperty::class.java, result.id, EntityEventType.UPDATE, mutableMapOf())
        return result
    }

    fun getById(id: UUID) = pseudoPropertyAdapter.getById(id)

    fun getPseudoProperties(request: SearchRequest) = pseudoPropertyAdapter.getPseudoProperties(request)
}

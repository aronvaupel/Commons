package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.EntityChangeTracker
import com.ecommercedemo.common.application.event.EntityEvent
import com.ecommercedemo.common.application.event.EntityEventProducer
import com.ecommercedemo.common.application.event.EntityEventType
import com.ecommercedemo.common.controller.abstraction.request.CreateRequest
import com.ecommercedemo.common.controller.abstraction.request.SearchRequest
import com.ecommercedemo.common.controller.abstraction.request.UpdateRequest
import com.ecommercedemo.common.controller.abstraction.util.Retriever
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import com.ecommercedemo.common.model.abstraction.ExpandableBaseEntity
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.persistence.abstraction.IPseudoPropertyRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Suppress("UNCHECKED_CAST")
abstract class ServiceTemplate<T : BaseEntity>(
    private val adapter: IEntityPersistenceAdapter<T>,
    private val entityClass: KClass<T>,
    private val eventProducer: EntityEventProducer,
    private val objectMapper: ObjectMapper,
    private val pseudoPropertyRepository: EntityRepository<out BasePseudoProperty, UUID>,
    private val retriever: Retriever,
) : IService<T> {

    @Transactional
    override fun create(request: CreateRequest<T>): T {
        val newInstance = constructEntity(request.data::class.memberProperties.associateBy { it.name }) { name ->
            request.data::class.memberProperties.firstOrNull { it.name == name }?.getter?.call(request.data)
        }

        handlePseudoPropertiesIfPresent(newInstance, request.data)

        return saveAndEmitEvent(newInstance, EntityEventType.CREATE, null)
    }

    @Transactional
    override fun createByEvent(event: EntityEvent<T>) {
        val newInstance = constructEntity(event.properties) { name ->
            event.properties[name]
        }

        handlePseudoPropertiesIfPresent(newInstance, event.properties)

        adapter.save(newInstance)
    }

    @Transactional
    override fun update(request: UpdateRequest): T {
        val originalEntity = getSingle(request.id)

        if (originalEntity != entityClass) {
            throw IllegalArgumentException(
                "Entity type mismatch. Expected ${entityClass.simpleName} but found ${originalEntity::class.java.simpleName}."
            )
        }

        val updatedEntity = mapPropertiesToEntity(originalEntity.copy() as T, request.properties)

        handlePseudoPropertiesIfPresent(updatedEntity, request.properties)

        return saveAndEmitEvent(updatedEntity, EntityEventType.UPDATE, originalEntity)
    }

    @Transactional
    override fun updateByEvent(event: EntityEvent<T>) {
        val originalEntity = getSingle(event.id)

        val updatedEntity = mapPropertiesToEntity(originalEntity.copy() as T, event.properties)

        handlePseudoPropertiesIfPresent(updatedEntity, event.properties)

        adapter.save(updatedEntity)
    }

    @Transactional
    override fun delete(id: UUID): HttpStatus {
        return try {
            val entity = handleMissingEntity { getSingle(id) } ?: return HttpStatus.NOT_FOUND
            adapter.delete(entity.id)
            eventProducer.emit(
                entity::class.java,
                id,
                EntityEventType.DELETE,
                mutableMapOf()
            )
            HttpStatus.OK
        } catch (e: Exception) {
            println("Error deleting entity with ID $id: ${e.message}")
            e.printStackTrace()
            HttpStatus.INTERNAL_SERVER_ERROR
        }
    }

    @Transactional
    override fun deleteByEvent(event: EntityEvent<T>) {
        try {
            val entity = handleMissingEntity { getSingle(event.id) }
            if (entity != null) {
                adapter.delete(entity.id)
            }
        } catch (e: Exception) {
            println("Error deleting entity with ID ${event.id}: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun getSingle(id: UUID): T = adapter.getById(id)

    override fun getMultiple(ids: List<UUID>): List<T> = adapter.getAllByIds(ids)

    override fun search(request: SearchRequest): List<T> = retriever.executeSearch(request, entityClass)

    private fun saveAndEmitEvent(entity: T, eventType: EntityEventType, originalEntity: T?): T {
        val savedEntity = adapter.save(entity)

        val changes =
            originalEntity?.let { EntityChangeTracker<T>().getChangedProperties(it, savedEntity) } ?: mutableMapOf()

        eventProducer.emit(entityClass.java, savedEntity.id, eventType, changes)

        return savedEntity
    }

    private fun validatePseudoPropertiesFromRequest(
        updatedEntity: ExpandableBaseEntity, pseudoPropertiesFromRequest: Map<String, Any?>
    ) {
        val validPseudoProperties: Map<String, Any> = getValidPseudoProperties(updatedEntity)

        pseudoPropertiesFromRequest.forEach { (key, value) ->
            val expectedType = validPseudoProperties[key]
                ?: throw IllegalArgumentException("Invalid pseudo-property: $key")

            if (value != null && value::class.qualifiedName != expectedType::class.qualifiedName) {
                throw IllegalArgumentException("Pseudo-property $key must be of type $expectedType")
            }
        }
    }

    private fun getValidPseudoProperties(updatedEntity: ExpandableBaseEntity): Map<String, Any> {
        if (pseudoPropertyRepository is IPseudoPropertyRepository<*>) {
            return pseudoPropertyRepository
                .findAllByEntitySimpleName(updatedEntity::class.simpleName!!)
                .associateBy { it.key }
                .mapValues {
                    objectMapper.readValue(it.value.typeDescriptor, object : TypeReference<Any>() {})
                }
        }
        throw IllegalStateException("Repository must implement IPseudoPropertyRepository")
    }

    private fun handlePseudoPropertiesIfPresent(entity: T, source: Any?) {
        if ((source is ExpandableBaseEntity && source.pseudoProperties.isNotEmpty()) ||
            (source is Map<*, *> && source.containsKey("pseudoProperties"))
        ) {
            handlePseudoProperties(entity, source)
        }
    }

    private fun <R> handleMissingEntity(action: () -> R): R? {
        return try {
            action()
        } catch (e: NoSuchElementException) {
            println("Error: Entity not found. Cannot proceed with the operation.")
            null
        }
    }

    private fun constructEntity(
        propertyMap: Map<String, Any?>,
        valueProvider: (String) -> Any?
    ): T {
        val entityConstructor = entityClass.constructors.firstOrNull()
            ?: throw IllegalArgumentException("No suitable constructor found for ${entityClass.simpleName}")

        val entityConstructorParams = entityConstructor.parameters.associateWith { param ->
            val value = valueProvider(param.name!!)
            if (value == null && !param.type.isMarkedNullable) {
                throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
            }
            value
        }

        val newInstance = entityConstructor.callBy(entityConstructorParams)

        val targetPropertyMap = newInstance::class.memberProperties.associateBy { it.name }

        targetPropertyMap.values
            .filterIsInstance<KMutableProperty<*>>()
            .forEach { property ->
                property.isAccessible = true
                val resolvedValue = valueProvider(property.name.removePrefix("_"))

                if (resolvedValue != null) {
                    property.setter.call(newInstance, resolvedValue)
                }
            }

        return newInstance
    }

    private fun handlePseudoProperties(entity: T, source: Any?) {
        if (entity !is ExpandableBaseEntity) {
            return
        }

        val pseudoPropertiesFromSource: Map<String, Any?> = when (source) {
            is ExpandableBaseEntity -> objectMapper.readValue(
                source.pseudoProperties, object : TypeReference<Map<String, Any?>>() {}
            )

            is Map<*, *> -> source["pseudoProperties"] as? Map<String, Any?> ?: emptyMap()
            else -> emptyMap()
        }

        if (pseudoPropertiesFromSource.isNotEmpty()) {
            validatePseudoPropertiesFromRequest(entity, pseudoPropertiesFromSource)

            val existingPseudoProperties: Map<String, Any?> = objectMapper.readValue(
                entity.pseudoProperties, object : TypeReference<Map<String, Any?>>() {}
            )
            val mergedPseudoProperties = existingPseudoProperties + pseudoPropertiesFromSource
            entity.pseudoProperties = objectMapper.writeValueAsString(mergedPseudoProperties)
        }
    }

    private fun mapPropertiesToEntity(entity: T, properties: Map<String, Any?>): T {
        val targetPropertyMap = entity::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .associateBy { it.name }

        targetPropertyMap.forEach { (name, property) ->
            property.isAccessible = true

            val resolvedValue =properties[name] ?: properties[name.removePrefix("_")]

            when {
                resolvedValue == null && !property.returnType.isMarkedNullable -> {
                    throw IllegalArgumentException("Field $name cannot be set to null.")
                }

                resolvedValue != null && resolvedValue::class.createType() != property.returnType -> {
                    throw IllegalArgumentException("Field $name must be of type ${property.returnType}")
                }

                resolvedValue != null -> {
                    property.setter.call(entity, resolvedValue)
                }
            }
        }

        return entity
    }
}

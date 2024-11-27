package com.ecommercedemo.common.service.utility

import com.ecommercedemo.common.application.EntityChangeTracker
import com.ecommercedemo.common.application.event.EntityEventProducer
import com.ecommercedemo.common.application.event.EntityEventType
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import com.ecommercedemo.common.model.abstraction.ExpandableBaseEntity
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import com.ecommercedemo.common.persistence.abstraction.IEntityPersistenceAdapter
import com.ecommercedemo.common.persistence.abstraction.IPseudoPropertyRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Suppress("UNCHECKED_CAST")
@Service
class ServiceUtility<T: BaseEntity>(
    private val adapter: IEntityPersistenceAdapter<T>,
    private val entityClass: KClass<T>,
    private val eventProducer: EntityEventProducer,
    private val objectMapper: ObjectMapper,
    private val pseudoPropertyRepository: EntityRepository<out BasePseudoProperty, UUID>,
) {
    fun saveAndEmitEvent(entity: T, eventType: EntityEventType, originalEntity: T?): T {
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

    fun handlePseudoPropertiesIfPresent(entity: T, source: Any?) {
        if ((source is ExpandableBaseEntity && source.pseudoProperties.isNotEmpty()) ||
            (source is Map<*, *> && source.containsKey("pseudoProperties"))
        ) {
            handlePseudoProperties(entity, source)
        }
    }

    fun <R> handleMissingEntity(action: () -> R): R? {
        return try {
            action()
        } catch (e: NoSuchElementException) {
            println("Error: Entity not found. Cannot proceed with the operation.")
            null
        }
    }

    fun constructEntity(
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
                val value = propertyMap[property.name.removePrefix("_")]
                if (value != null) {
                    property.setter.call(newInstance, value)
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

    fun mapPropertiesToEntity(entity: T, properties: Map<String, Any?>): T {
        val targetPropertyMap = entity::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .associateBy { it.name }

        targetPropertyMap.forEach { (name, property) ->
            val value = properties[name.removePrefix("_")]
            property.isAccessible = true

            when {
                value == null && !property.returnType.isMarkedNullable -> {
                    throw IllegalArgumentException("Field $name cannot be set to null.")
                }

                value != null && value::class.createType() != property.returnType -> {
                    throw IllegalArgumentException("Field $name must be of type ${property.returnType}")
                }

                value != null -> {
                    property.setter.call(entity, value)
                }
            }
        }

        return entity
    }
}
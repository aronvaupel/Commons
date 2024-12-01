package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import com.ecommercedemo.common.model.abstraction.ExpandableBaseEntity
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
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

@Service
@Suppress("UNCHECKED_CAST")
class ServiceUtility(
    private val objectMapper: ObjectMapper,
    private val _pseudoPropertyRepository: EntityRepository<out BasePseudoProperty, UUID>,
) {
    private fun getValidPseudoProperties(updatedEntity: ExpandableBaseEntity): Map<String, Any> {
        if (_pseudoPropertyRepository is IPseudoPropertyRepository<*>) {
            return _pseudoPropertyRepository
                .findAllByEntitySimpleName(updatedEntity::class.simpleName!!)
                .associateBy { it.key }
                .mapValues {
                    objectMapper.readValue(it.value.typeDescriptor, object : TypeReference<Any>() {})
                }
        }
        throw IllegalStateException("Repository must implement IPseudoPropertyRepository")
    }

    fun <E: BaseEntity> handlePseudoPropertiesIfPresent(entity:E, source: Any?) {
        if ((source is ExpandableBaseEntity && source.pseudoProperties.isNotEmpty()) ||
            (source is Map<*, *> && source.containsKey("pseudoProperties"))
        ) {
            println("Handling pseudo-properties for entity: $entity")
            handlePseudoProperties(entity, source)
        }
    }


    fun <E: BaseEntity> instantiateEntity(
        instanceClass: KClass<E>,
        valueProvider: (String) -> Any?
    ): E {
        val entityConstructor = instanceClass.constructors.firstOrNull()
            ?: throw IllegalArgumentException("No suitable constructor found for ${instanceClass.simpleName}")

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
                    val finalValue = if (newInstance is BasePseudoProperty && property.name == "typeDescriptor" && resolvedValue !is String) {
                        ObjectMapper().writeValueAsString(resolvedValue)
                    } else resolvedValue
                    println("Property: ${property.name}, Expected: ${property.returnType}, Actual: ${resolvedValue.javaClass.name}")
                    println("Resolved Value: $resolvedValue, Final Value: $finalValue")
                    property.setter.call(newInstance, finalValue)
                }
            }

        return newInstance
    }

    private fun <E: BaseEntity> handlePseudoProperties(entity: E, source: Any?) {
        if (entity !is ExpandableBaseEntity) {
            return
        }
        println("Entering handlePseudoProperties")
        val pseudoPropertiesFromSource: Map<String, Any?> = when (source) {
            is ExpandableBaseEntity -> {
                val deserializedProperties = objectMapper.readValue(
                    source.pseudoProperties, object : TypeReference<Map<String, Any?>>() {}
                )
                println("Pseudo-properties from source: $deserializedProperties")
                deserializedProperties
            }

            is Map<*, *> -> {
                val resolvedSource = source["pseudoProperties"] as? Map<String, Any?> ?: emptyMap()
                println("Pseudo-properties from source: $resolvedSource")
                resolvedSource
            }
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

    fun <E: BaseEntity> applyPropertiesToExistingEntity(entity: E, properties: Map<String, Any?>): E {
        val entityProperties = entity::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .associateBy { it.name }

        properties.forEach { (key, value) ->
            val property = entityProperties[key] ?: entityProperties[key.removePrefix("_")]

            if (property == null) {
                throw IllegalArgumentException("Field $key does not exist in the entity.")
            }

            property.isAccessible = true

            when {
                value == null && !property.returnType.isMarkedNullable -> {
                    throw IllegalArgumentException("Field $key cannot be set to null.")
                }

                value != null && value::class.createType() != property.returnType -> {
                    throw IllegalArgumentException("Field $key must be of type ${property.returnType}.")
                }

                else -> {
                    property.setter.call(entity, value)
                }
            }
        }

        return entity
    }

    private fun validatePseudoPropertiesFromRequest(
        updatedEntity: ExpandableBaseEntity, pseudoPropertiesFromRequest: Map<String, Any?>
    ) {
        val validPseudoProperties: Map<String, Any> = getValidPseudoProperties(updatedEntity)
        println("Valid pseudo-properties: $validPseudoProperties")
        pseudoPropertiesFromRequest.forEach { (key, value) ->
            val expectedType = validPseudoProperties[key]
                ?: throw IllegalArgumentException("Invalid pseudo-property: $key")

            if (value != null && value::class.qualifiedName != expectedType::class.qualifiedName) {
                throw IllegalArgumentException("Pseudo-property $key must be of type $expectedType")
            }
        }
    }
}
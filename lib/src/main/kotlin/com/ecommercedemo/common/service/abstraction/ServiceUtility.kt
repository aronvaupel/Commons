package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
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

    fun <E : BaseEntity> createNewInstance(
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
                val deserializedJsonString = when (property.name) {
                    "pseudoProperties" -> deserializeJsonBProperty(property.getter.call(newInstance) as String)
                    "typeDescriptor" -> deserializeJsonBProperty(property.getter.call(newInstance) as String)
                    else -> null
                }

                val resolvedValue =
                    if (property.name == "pseudoProperties" || property.name == "typeDescriptor") deserializedJsonString
                    else valueProvider(property.name.removePrefix("_"))

                if (resolvedValue != null) {
                    println("Property: ${property.name}, Expected: ${property.returnType}, Actual: ${resolvedValue.javaClass.name}")
                    println("Resolved Value: $resolvedValue, Final Value: $resolvedValue")
                    property.setter.call(newInstance, resolvedValue)
                }
            }

        return newInstance
    }

    fun <E : BaseEntity> updateExistingInstance(entity: E, propertiesFromRequest: Map<String, Any?>): E {
        println("Attempting to apply properties to existing entity: $entity")
        val entityProperties = entity::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .associateBy { it.name }
        println("Entity properties: $entityProperties")

        propertiesFromRequest.forEach { (key, value) ->
            val property = entityProperties[key] ?: entityProperties[key.removePrefix("_")]
            println("Property: $property")

            if (property == null) {
                throw IllegalArgumentException("Field $key does not exist in the entity.")
            }

            property.isAccessible = true

            when {
                value == null && !property.returnType.isMarkedNullable -> {
                    throw IllegalArgumentException("Field $key cannot be set to null.")
                }

                key == "pseudoProperties" -> {
                    println("Handling pseudoProperties during applyProperties...")
                    if (entity is ExpandableBaseEntity) {
                        val pseudoPropertiesFromSource = value as? Map<String, Any?>
                            ?: throw IllegalArgumentException("pseudoProperties must be a Map<String, Any?>")

                        validatePseudoPropertiesFromRequest(entity, pseudoPropertiesFromSource)
                        val existingPseudoProperties = deserializeJsonBProperty(entity.pseudoProperties)
                        val mergedPseudoProperties =
                            mergePseudoProperties(existingPseudoProperties, pseudoPropertiesFromSource)
                        property.setter.call(entity, mergedPseudoProperties)
                    } else {
                        throw IllegalArgumentException("Entity does not support pseudoProperties")
                    }
                }

                key == "typeDescriptor" -> {
                    println("Handling typeDescriptor during applyProperties...")
                    if (entity is BasePseudoProperty) {
                        val typeDescriptor = value as TypeDescriptor

                        val typeDescriptorAsString = objectMapper.writeValueAsString(typeDescriptor)
                        property.setter.call(entity, typeDescriptorAsString)
                    } else {
                        throw IllegalArgumentException("Entity does not support typeDescriptor")
                    }
                }

                value != null
                        && key != "typeDescriptor"
                        && key != "pseudoProperties"
                        && value::class.createType() != property.returnType -> {
                    throw IllegalArgumentException("Field $key must be of type ${property.returnType}.")
                }

                else -> {
                    println("Attempting to set property $key to value $value")
                    property.setter.call(entity, value)
                }
            }
        }

        return entity
    }

    private fun deserializeJsonBProperty(pseudoPropertiesAsString: String): Map<String, Any?> {
        return try {
            objectMapper.readValue(pseudoPropertiesAsString, object : TypeReference<Map<String, Any?>>() {})
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to deserialize pseudoProperties for entity: ${e.message}", e)
        }
    }

    private fun validatePseudoPropertiesFromRequest(
        updatedEntity: ExpandableBaseEntity,
        pseudoPropertiesFromRequest: Map<String, Any?>
    ) {
        val validPseudoProperties = getValidPseudoProperties(updatedEntity)
        println("Valid pseudo-properties: $validPseudoProperties")

        pseudoPropertiesFromRequest.forEach { (key, value) ->
            val expectedType = validPseudoProperties[key]
                ?: throw IllegalArgumentException("Invalid pseudo-property: $key")

            if (value != null && value::class.qualifiedName != expectedType::class.qualifiedName) {
                throw IllegalArgumentException("Pseudo-property $key must be of type $expectedType")
            }
        }
    }

    private fun mergePseudoProperties(
        existing: Map<String, Any?>,
        updates: Map<String, Any?>
    ): String {
        return objectMapper.writeValueAsString(existing + updates)
    }

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
}

package com.ecommercedemo.common.service.abstraction

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import com.ecommercedemo.common.model.abstraction.ExpandableBaseEntity
import com.ecommercedemo.common.persistence.abstraction.EntityRepository
import com.ecommercedemo.common.persistence.abstraction.IPseudoPropertyRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
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
        println("Entity constructor: $entityConstructor")

        val entityConstructorParams = entityConstructor.parameters.associateWith { param ->
            val value = valueProvider(param.name!!)
            if (value == null && !param.type.isMarkedNullable) {
                throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
            }
            value
        }
        println("Entity constructor parameters: $entityConstructorParams")

        val newInstance = entityConstructor.callBy(entityConstructorParams)
        println("New instance: $newInstance")

        val targetPropertyMap = newInstance::class.memberProperties.associateBy { it.name }
        println("Target property map: $targetPropertyMap")

        targetPropertyMap.values
            .filterIsInstance<KMutableProperty<*>>()
            .forEach { property ->
                property.isAccessible = true

                val resolvedValue = valueProvider(property.name.removePrefix("_"))
                println("Resolved value for property ${property.name}: $resolvedValue")

                when {
                    resolvedValue == null && !property.returnType.isMarkedNullable -> {
                        throw IllegalArgumentException("Field '${property.name}' is non-nullable and cannot be set to null.")
                    }

                    property.name == ExpandableBaseEntity::pseudoProperties.name -> {
                        if (newInstance is ExpandableBaseEntity) {
                            val requestPseudoProperties = resolvedValue as? Map<String, Any?>
                                ?: emptyMap()

                            if (requestPseudoProperties.isNotEmpty()) {
                                println("Request pseudo-properties: $requestPseudoProperties")

                                validatePseudoPropertiesFromRequest(newInstance, requestPseudoProperties)

                                val existingPseudoProperties = deserializeJsonBProperty(newInstance.pseudoProperties)
                                val mergedPseudoProperties = mergePseudoProperties(existingPseudoProperties, requestPseudoProperties)

                                property.setter.call(newInstance, mergedPseudoProperties)
                            }
                        } else {
                            throw IllegalArgumentException("Entity does not support pseudoProperties")
                        }
                    }
                    //Todo: rethink this. Sending TypeDescriptor as a string is not ideal. Deserializing and serializing again is also not cool. Maybe change CreateRequest?
                    property.name == BasePseudoProperty::typeDescriptor.name -> {
                        val typeDescriptorObject = try {
                            objectMapper.readValue(resolvedValue.toString(), TypeDescriptor::class.java)
                        } catch (e: Exception) {
                            throw IllegalArgumentException("Failed to deserialize typeDescriptor: ${e.message}", e)
                        }
                        println("Successfully deserialized TypeDescriptor: $typeDescriptorObject")

                        val typeDescriptorAsString = objectMapper.writeValueAsString(typeDescriptorObject)
                        property.setter.call(newInstance, typeDescriptorAsString)
                    }

                    resolvedValue != null && resolvedValue::class.createType() != property.returnType -> {
                        throw IllegalArgumentException(
                            "Type mismatch for property '${property.name}': " +
                                    "Expected ${property.returnType}, Found ${resolvedValue::class.createType()}"
                        )
                    }

                    else -> {
                        property.setter.call(newInstance, resolvedValue)
                    }
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
            val correspondingEntityProperty = entityProperties[key] ?: entityProperties[key.removePrefix("_")]
            println("Property from request: $key corresponds to property: $correspondingEntityProperty")

            if (correspondingEntityProperty == null) {
                throw IllegalArgumentException("Field $key does not exist in the entity.")
            }

            correspondingEntityProperty.isAccessible = true

            when {
                value == null && !correspondingEntityProperty.returnType.isMarkedNullable -> {
                    throw IllegalArgumentException("Field $key cannot be set to null.")
                }

                key == ExpandableBaseEntity::pseudoProperties.name -> {
                    println("Handling pseudoProperties during applyProperties...")
                    if (entity is ExpandableBaseEntity) {
                        val pseudoPropertiesFromSource = value as? Map<String, Any?>
                            ?: throw IllegalArgumentException("pseudoProperties must be a Map<String, Any?>")

                        validatePseudoPropertiesFromRequest(entity, pseudoPropertiesFromSource)
                        val existingPseudoProperties = deserializeJsonBProperty(entity.pseudoProperties)
                        println("Deserialized existing pseudo-properties: $existingPseudoProperties")
                        val mergedPseudoProperties =
                            mergePseudoProperties(existingPseudoProperties, pseudoPropertiesFromSource)
                        correspondingEntityProperty.setter.call(entity, mergedPseudoProperties)
                        println("Merged pseudo-properties: $mergedPseudoProperties")
                    } else {
                        throw IllegalArgumentException("Entity does not support pseudoProperties")
                    }
                }

                key == BasePseudoProperty::typeDescriptor.name -> {
                    println("Handling typeDescriptor during applyProperties...")
                    if (entity is BasePseudoProperty) {
                        val typeDescriptor = value as TypeDescriptor

                        val typeDescriptorAsString = objectMapper.writeValueAsString(typeDescriptor)
                        correspondingEntityProperty.setter.call(entity, typeDescriptorAsString)
                    } else {
                        throw IllegalArgumentException("Entity does not support typeDescriptor")
                    }
                }

                value != null
                        && key != BasePseudoProperty::typeDescriptor.name
                        && key != ExpandableBaseEntity::pseudoProperties.name
                        && value::class.createType() != correspondingEntityProperty.returnType -> {
                    throw IllegalArgumentException("Field $key must be of type ${correspondingEntityProperty.returnType}.")
                }

                else -> {
                    println("Attempting to set property $key to value $value")
                    correspondingEntityProperty.setter.call(entity, value)
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
            val registeredPseudoProperty = validPseudoProperties.firstOrNull { it.key == key }
                ?: throw IllegalArgumentException("Invalid pseudo-property: $key")
            println("Registered pseudo-property: $registeredPseudoProperty")

            val registeredPseudoPropertyTypeDescriptor =
                objectMapper.convertValue<TypeDescriptor>(registeredPseudoProperty.typeDescriptor)
            println("Registered pseudo-property type descriptor: $registeredPseudoPropertyTypeDescriptor")

            if (!ValueType.validateValueAgainstDescriptor(registeredPseudoPropertyTypeDescriptor, value)) {
                throw IllegalArgumentException(
                    "Pseudo-property '$key' does not match the expected type. " +
                            "Descriptor: ${registeredPseudoPropertyTypeDescriptor.type.typeInfo}, Found: ${value?.javaClass?.name}"
                )
            }
        }
    }

    private fun mergePseudoProperties(
        existing: Map<String, Any?>,
        updates: Map<String, Any?>
    ): String {
        return objectMapper.writeValueAsString(existing + updates)
    }

    private fun getValidPseudoProperties(entity: ExpandableBaseEntity): List<BasePseudoProperty> {
        if (_pseudoPropertyRepository is IPseudoPropertyRepository<*>) {
            return _pseudoPropertyRepository.findAllByEntitySimpleName(entity::class.simpleName!!)
        }
        throw IllegalStateException("Repository must implement IPseudoPropertyRepository")
    }
}

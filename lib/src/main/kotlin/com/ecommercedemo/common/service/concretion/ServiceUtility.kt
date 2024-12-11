package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import com.ecommercedemo.common.persistence.concretion._pseudoProperty._PseudoPropertyRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Service
@Suppress("UNCHECKED_CAST")
class ServiceUtility(
    private val objectMapper: ObjectMapper,
    private val _pseudoPropertyRepository: _PseudoPropertyRepository,
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

                val resolvedValue = valueProvider(property.name.removePrefix("_"))

                when {
                    resolvedValue == null && !property.returnType.isMarkedNullable -> {
                        throw IllegalArgumentException("Field '${property.name}' is non-nullable and cannot be set to null.")
                    }

                    property.name == AugmentableBaseEntity::pseudoProperties.name -> {
                        if (newInstance is AugmentableBaseEntity) {
                            val pseudoPropertiesValue = valueProvider(AugmentableBaseEntity::pseudoProperties.name)
                            val pseudoPropertiesFromRequest = when (pseudoPropertiesValue) {
                                is String -> {
                                    objectMapper.readValue(
                                        pseudoPropertiesValue,
                                        object : TypeReference<Map<String, Any?>>() {})
                                }

                                is Map<*, *> -> {
                                    pseudoPropertiesValue as Map<String, Any?>
                                }

                                else -> {
                                    throw IllegalArgumentException("Invalid pseudoProperties format: $pseudoPropertiesValue")
                                }

                            }
                            validatePseudoPropertiesFromRequest(newInstance, pseudoPropertiesFromRequest)

                            val existingPseudoProperties = deserializePseudoProperty(newInstance.pseudoProperties)
                            val mergedPseudoProperties =
                                mergePseudoProperties(existingPseudoProperties, pseudoPropertiesFromRequest)

                            property.setter.call(newInstance, mergedPseudoProperties)
                        }
                    }
                    property.name == BasePseudoProperty::typeDescriptor.name -> {
                        val typeDescriptorObject = try {
                            objectMapper.readValue(resolvedValue.toString(), TypeDescriptor::class.java)
                        } catch (e: Exception) {
                            throw IllegalArgumentException("Failed to deserialize typeDescriptor: ${e.message}", e)
                        }

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
        val entityProperties = entity::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .associateBy { it.name }

        propertiesFromRequest.forEach { (key, value) ->
            val correspondingEntityProperty = entityProperties[key] ?: entityProperties[key.removePrefix("_")]

            if (correspondingEntityProperty == null) {
                throw IllegalArgumentException("Field $key does not exist in the entity.")
            }

            correspondingEntityProperty.isAccessible = true

            when {
                value == null && !correspondingEntityProperty.returnType.isMarkedNullable -> {
                    throw IllegalArgumentException("Field $key cannot be set to null.")
                }

                key == AugmentableBaseEntity::pseudoProperties.name -> {
                    if (entity is AugmentableBaseEntity) {
                        val pseudoPropertiesFromSource = value as? Map<String, Any?>
                            ?: throw IllegalArgumentException("pseudoProperties must be a Map<String, Any?>")
                        validatePseudoPropertiesFromRequest(entity, pseudoPropertiesFromSource)
                        val existingPseudoProperties = deserializePseudoProperty(entity.pseudoProperties)
                        val mergedPseudoProperties =
                            mergePseudoProperties(existingPseudoProperties, pseudoPropertiesFromSource)
                        correspondingEntityProperty.setter.call(entity, mergedPseudoProperties)
                    } else {
                        throw IllegalArgumentException("Entity does not support pseudoProperties")
                    }
                }

                key == BasePseudoProperty::typeDescriptor.name -> {
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
                        && key != AugmentableBaseEntity::pseudoProperties.name
                        && value::class.createType() != correspondingEntityProperty.returnType -> {
                    throw IllegalArgumentException("Field $key must be of type ${correspondingEntityProperty.returnType}.")
                }

                else -> {
                    correspondingEntityProperty.setter.call(entity, value)
                }
            }
        }

        return entity
    }

    fun deserializePseudoProperty(asString: String): Map<String, Any?> {
        return try {
            objectMapper.readValue(asString, object : TypeReference<Map<String, Any?>>() {})
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to deserialize pseudoProperties for entity: ${e.message}", e)
        }
    }

    private fun validatePseudoPropertiesFromRequest(
        updatedEntity: AugmentableBaseEntity,
        pseudoPropertiesFromRequest: Map<String, Any?>
    ) {
        val validPseudoProperties = getValidPseudoProperties(updatedEntity)

        val existingPseudoProperties = deserializePseudoProperty(updatedEntity.pseudoProperties)

        val requiredPseudoProperties = validPseudoProperties.filter {
            when (val typeDescriptor = objectMapper.readValue(it.typeDescriptor, TypeDescriptor::class.java)) {
                is TypeDescriptor.CollectionDescriptor, is TypeDescriptor.MapDescriptor ->
                    typeDescriptor.hasMinElementsOrEntries()

                else -> !typeDescriptor.isNullable()
            }
        }

        val missingPseudoProperties = requiredPseudoProperties.filterNot {
            pseudoPropertiesFromRequest.containsKey(it.key)
        }.filterNot { requiredProperty ->
            existingPseudoProperties.containsKey(requiredProperty.key) || pseudoPropertiesFromRequest.containsKey(
                requiredProperty.key
            )
        }

        if (missingPseudoProperties.isNotEmpty()) {
            throw IllegalArgumentException(
                "Missing required pseudo-properties: ${missingPseudoProperties.joinToString(", ") { it.key }}"
            )
        }

        val validationErrors = pseudoPropertiesFromRequest.mapNotNull { (key, value) ->
            val registeredPseudoProperty = validPseudoProperties.firstOrNull { it.key == key }
            if (registeredPseudoProperty == null) {
                "Pseudo-property '$key' is not registered for this entity."
            } else
                registeredPseudoProperty.let {
                    val typeDescriptor = objectMapper.readValue(it.typeDescriptor, TypeDescriptor::class.java)
                    val failureDetails = mutableListOf<String>()
                    val isValid = try {
                        ValueType.validateValueAgainstDescriptor(
                            typeDescriptor,
                            value,
                            failureDetails
                        )
                    } catch (e: Exception) {
                        failureDetails.add("Validation error for pseudo-property '$key': ${e.message}")
                        false
                    }
                    if (!isValid) {
                        "Pseudo-property '$key' does not match the expected type or constraints. Descriptor: $typeDescriptor,\n\n Value: $value.\n" +
                                "\n Details: ${failureDetails.joinToString("; ")}"
                    } else null
                }
        }


        if (validationErrors.isNotEmpty()) {
            throw IllegalArgumentException(
                "Pseudo-property validation failed with the following errors:\n${
                    validationErrors.joinToString(
                        "\n"
                    )
                }"
            )
        }
    }

    private fun TypeDescriptor.isNullable() = when (this) {
        is TypeDescriptor.PrimitiveDescriptor -> isNullable
        is TypeDescriptor.TimeDescriptor -> isNullable
        is TypeDescriptor.ComplexObjectDescriptor -> isNullable
        else -> true
    }

    private fun TypeDescriptor.hasMinElementsOrEntries() = when (this) {
        is TypeDescriptor.CollectionDescriptor -> minElements > 0
        is TypeDescriptor.MapDescriptor -> minEntries > 0
        else -> false
    }

    private fun mergePseudoProperties(
        existing: Map<String, Any?>,
        updates: Map<String, Any?>
    ): String {
        val result = objectMapper.writeValueAsString(existing + updates)
        return result
    }

    private fun getValidPseudoProperties(entity: AugmentableBaseEntity): List<BasePseudoProperty> {
        return _pseudoPropertyRepository.findAllByEntitySimpleName(entity::class.simpleName!!)
    }
}

package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import com.ecommercedemo.common.persistence.concretion._pseudoProperty._PseudoPropertyRepository
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
        properties: Map<String, Any?>,
    ): E {
        val entityConstructor = instanceClass.constructors.firstOrNull()
            ?: throw IllegalArgumentException("No suitable constructor found for ${instanceClass.simpleName}")

        val targetPropertyMap = instanceClass::class.memberProperties.associateBy { it.name }

        val resolvedProperties = properties.mapKeys { (key, _) ->
            targetPropertyMap.values
                .firstOrNull { it.name == key || it.name.removePrefix("_") == key }?.name
                ?: throw IllegalArgumentException("Field $key does not exist in the entity.")
        }.let { mappedProperties ->
            objectMapper.readValue(objectMapper.writeValueAsString(mappedProperties), instanceClass.java)
        }::class.memberProperties.associateBy { it.name }.mapValues { (name, property) ->
            properties[name.removePrefix("_")] ?: property.getter.call()
        }
        println("resolvedProperties: $resolvedProperties")

        val entityConstructorParams = entityConstructor.parameters.associateWith { param ->
            resolvedProperties[param.name] ?: resolvedProperties[param.name?.removePrefix("_")]
            ?: if (!param.type.isMarkedNullable && !param.isOptional) {
                throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
            } else null
        }

        val newInstance = entityConstructor.callBy(entityConstructorParams)

        targetPropertyMap.values
            .filterIsInstance<KMutableProperty<*>>()
            .onEach { it.isAccessible = true }
            .forEach { targetProperty ->

                val resolvedValueFromRequest =
                    resolvedProperties[targetProperty.name]
                        ?: resolvedProperties[targetProperty.name.removePrefix("_")]

                when {
                    resolvedValueFromRequest == null && (!targetProperty.returnType.isMarkedNullable) -> {
                        throw IllegalArgumentException(
                            "Field '${targetProperty.name}' is non-nullable and cannot be set to null."
                        )
                    }

                    targetProperty.name == AugmentableBaseEntity::pseudoProperties.name -> {
                        if (newInstance is AugmentableBaseEntity) {
                            if (resolvedValueFromRequest == null)
                                return@forEach
                            validatePseudoPropertiesFromRequest(newInstance, resolvedValueFromRequest)
                            val serialized = serialize(resolvedValueFromRequest)
                            targetProperty.setter.call(newInstance, serialized)
                        } else throw IllegalArgumentException("Entity does not support pseudoProperties")
                    }

                    targetProperty.name == BasePseudoProperty::typeDescriptor.name -> {
                        if (newInstance is BasePseudoProperty) {
                            validateTypeDescriptor(resolvedValueFromRequest)
                            val serialized = serialize(resolvedValueFromRequest as TypeDescriptor)
                            targetProperty.setter.call(newInstance, serialized)
                        } else throw IllegalArgumentException("Entity does not support typeDescriptor")
                    }

                    resolvedValueFromRequest != null
                            && resolvedValueFromRequest::class.createType() != targetProperty.returnType -> {
                        throw IllegalArgumentException(
                            "Type mismatch for property '${targetProperty.name}': " +
                                    "Expected ${targetProperty.returnType}, " +
                                    "Found ${resolvedValueFromRequest::class.createType()}"
                        )
                    }

                    else -> {
                        targetProperty.setter.call(newInstance, resolvedValueFromRequest)
                    }
                }
            }

        return newInstance
    }

    fun <E : BaseEntity> updateExistingInstance(entity: E, propertiesFromRequest: Map<String, Any?>): E {
        val targetEntityProperties = entity::class.memberProperties
            .filterIsInstance<KMutableProperty<*>>()
            .associateBy { it.name }

        propertiesFromRequest.forEach { (key, value) ->
            val correspondingTargetProperty = targetEntityProperties[key]
                ?: targetEntityProperties[key.removePrefix("_")]

            if (correspondingTargetProperty == null) {
                throw IllegalArgumentException("Field $key does not exist in the entity.")
            }

            correspondingTargetProperty.isAccessible = true

            when {
                value == null && !correspondingTargetProperty.returnType.isMarkedNullable -> {
                    throw IllegalArgumentException("Field $key cannot be set to null.")
                }

                key == AugmentableBaseEntity::pseudoProperties.name -> {
                    if (entity is AugmentableBaseEntity) {
                        validatePseudoPropertiesFromRequest(entity, value)
                        val existingPseudoProperties = entity.getPseudoPropertiesDeserialized()
                        val mergedPseudoProperties =
                            mergePseudoProperties(existingPseudoProperties, value as Map<String, Any?>)
                        correspondingTargetProperty.setter.call(entity, mergedPseudoProperties)
                    } else {
                        throw IllegalArgumentException("Entity does not support pseudoProperties")
                    }
                }

                key == BasePseudoProperty::typeDescriptor.name -> {
                    if (entity is BasePseudoProperty) {
                        validateTypeDescriptor(value)
                        val serialized = serialize(value as TypeDescriptor)
                        correspondingTargetProperty.setter.call(entity, serialized)
                    } else {
                        throw IllegalArgumentException("Entity does not support typeDescriptor")
                    }
                }

                value != null
                        && key != BasePseudoProperty::typeDescriptor.name
                        && key != AugmentableBaseEntity::pseudoProperties.name
                        && value::class.createType() != correspondingTargetProperty.returnType -> {
                    throw IllegalArgumentException(
                        "Field $key must be of type ${correspondingTargetProperty.returnType}."
                    )
                }

                else -> {
                    correspondingTargetProperty.setter.call(entity, value)
                }
            }
        }

        return entity
    }

    private fun validateTypeDescriptor(value: Any?) {
        if (value == null)
            throw IllegalArgumentException("TypeDescriptor must be provided")

        if (value !is TypeDescriptor)
            throw IllegalArgumentException("TypeDescriptor must be a TypeDescriptor")
    }

    private fun validatePseudoPropertiesFromRequest(
        updatedEntity: AugmentableBaseEntity,
        pseudoPropertiesFromRequest: Any?
    ) {
        if (pseudoPropertiesFromRequest !is Map<*, *>)
            throw IllegalArgumentException("pseudoProperties must be a Map")

        val asTypesafeMap = pseudoPropertiesFromRequest.entries.associate { (key, value) ->
            if (key !is String) throw IllegalArgumentException("PseudoProperty-keys must be Strings")
            key to value
        }

        val validPseudoProperties = getValidPseudoProperties(updatedEntity)

        val existingPseudoProperties = updatedEntity.getPseudoPropertiesDeserialized()

        val requiredPseudoProperties = validPseudoProperties.filter {
            when (val typeDescriptor = it.getTypeDescriptorDeserialized()) {
                is TypeDescriptor.CollectionDescriptor, is TypeDescriptor.MapDescriptor ->
                    typeDescriptor.hasMinElementsOrEntries()

                else -> !typeDescriptor.isNullable()
            }
        }

        val missingPseudoProperties = requiredPseudoProperties.filterNot {
            asTypesafeMap.containsKey(it.key)
        }.filterNot { requiredProperty ->
            existingPseudoProperties.containsKey(requiredProperty.key) || asTypesafeMap.containsKey(
                requiredProperty.key
            )
        }

        if (missingPseudoProperties.isNotEmpty()) {
            throw IllegalArgumentException(
                "Missing required pseudo-properties: ${missingPseudoProperties.joinToString(", ") { it.key }}"
            )
        }

        val validationErrors = asTypesafeMap.mapNotNull { (key, value) ->
            val registeredPseudoProperty = validPseudoProperties.firstOrNull { it.key == key }

            if (registeredPseudoProperty == null) {
                "Pseudo-property '$key' is not registered for this entity."
            } else
                registeredPseudoProperty.let {
                    val typeDescriptor = it.getTypeDescriptorDeserialized()
                    val failureDetails = mutableListOf<String>()
                    val isValid = try {
                        ValueType.validateValueAgainstDescriptor(typeDescriptor, value, failureDetails)
                    } catch (e: Exception) {
                        failureDetails.add("Validation error for pseudo-property '$key': ${e.message}")
                        false
                    }

                    if (!isValid) {
                        "Pseudo-property '$key' does not match the expected type or constraints. Descriptor: " +
                                "$typeDescriptor,\n\n Value: $value.\n" +
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

    private fun mergePseudoProperties(existing: Map<String, Any?>, updates: Map<String, Any?>)
    = serialize(existing + updates)


    private fun getValidPseudoProperties(entity: AugmentableBaseEntity): List<BasePseudoProperty> {
        return _pseudoPropertyRepository.findAllByEntitySimpleName(entity::class.simpleName!!)
    }

    private fun serialize(value: Any): String {
        return objectMapper.writeValueAsString(value)
    }
}

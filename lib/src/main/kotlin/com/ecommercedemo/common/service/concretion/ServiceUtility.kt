package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.IPseudoProperty
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
class ServiceUtility<T : BaseEntity>(
    private val objectMapper: ObjectMapper,
    private val _pseudoPropertyRepository: _PseudoPropertyRepository,
    private val typeReAttacher: TypeReAttacher,
) {

    fun createNewInstance(
        instanceClass: KClass<T>,
        data: Map<String, Any?>,
    ): T {
        println("DATA IN CREATE NEW INSTANCE: $data")
        val entityConstructor = instanceClass.constructors.find { it.parameters.isNotEmpty() }
            ?: throw IllegalArgumentException("No suitable constructor found for ${instanceClass.simpleName}")

        val instanceConstructorParams = entityConstructor.parameters.associateWith { param ->
            println("PARAM: ${param.name}")
            val value = data[param.name?.removePrefix("_")]
            println("VALUE: $value")

            when {
                param.name == AugmentableBaseEntity::pseudoProperties.name
                        && data[AugmentableBaseEntity::pseudoProperties.name] != null -> {
                    if (instanceClass is AugmentableBaseEntity) {
                        validateDataAsPseudoProperties(
                            instanceClass as AugmentableBaseEntity,
                            data[AugmentableBaseEntity::pseudoProperties.name]
                        )
                        serialize(data[AugmentableBaseEntity::pseudoProperties.name]!!)
                    } else throw IllegalArgumentException("Entity does not support pseudoProperties")
                }

                param.name == IPseudoProperty::typeDescriptor.name
                        && data[IPseudoProperty::typeDescriptor.name] != null -> {
                    if (instanceClass is IPseudoProperty) {
                        validateTypeDescriptor(data[IPseudoProperty::typeDescriptor.name])
                        serialize(data[IPseudoProperty::typeDescriptor.name]!!)
                    } else throw IllegalArgumentException("Entity does not support typeDescriptor")
                }

                value != null -> value

                param.type.isMarkedNullable || param.isOptional -> null

                else -> throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
            }
        }.filter { !(it.value == null && !it.key.isOptional) }
        println("ENTITY CONSTRUCTOR PARAMS: $instanceConstructorParams")

        return entityConstructor.callBy(instanceConstructorParams)

//        val newInstance = entityConstructor.callBy(instanceConstructorParams)
//        println("NEW INSTANCE: $newInstance")


//        val instanceProperties = newInstance::class.memberProperties.associateBy { it.name }
//        println("TARGET PROPERTY MAP: $instanceProperties")

//        instanceProperties.values
//            .filterIsInstance<KMutableProperty<*>>()
//            .onEach { it.isAccessible = true }
//            .forEach { instanceProperty ->
//                println("PROPERTY: ${instanceProperty.name}")
//                val dataValue = data[instanceProperty.name.removePrefix("_")]
//                println("VALUE: $dataValue")

//                when {
//                    dataValue == null && (!instanceProperty.returnType.isMarkedNullable) -> {
//                        throw IllegalArgumentException(
//                            "Field '${instanceProperty.name}' is non-nullable and cannot be set to null."
//                        )
//                    }

//                    instanceProperty.name == AugmentableBaseEntity::pseudoProperties.name -> {
//                        if (data[AugmentableBaseEntity::pseudoProperties.name] == null) return@forEach
//                        if (newInstance is AugmentableBaseEntity) {
//                            validateDataAsPseudoProperties(
//                                newInstance, data[AugmentableBaseEntity::pseudoProperties.name]
//                            )
//                            val serialized = serialize(dataValue!!)
//                            instanceProperty.setter.call(newInstance, serialized)
//                        } else throw IllegalArgumentException("Entity does not support pseudoProperties")
//                    }

//                    instanceProperty.name == IPseudoProperty::typeDescriptor.name -> {
//                        if (newInstance is IPseudoProperty) {
//                            validateTypeDescriptor(data[IPseudoProperty::typeDescriptor.name])
//                            instanceProperty.setter.call(newInstance, dataValue)
//                        } else throw IllegalArgumentException("Entity does not support typeDescriptor")
//                    }

//                    dataValue != null && dataValue::class.createType() != instanceProperty.returnType -> {
//                        throw IllegalArgumentException(
//                            "Type mismatch for property '${instanceProperty.name}': " + "Expected ${instanceProperty.returnType}, " + "Found ${dataValue::class.createType()}"
//                        )
//                    }

//                    else -> instanceProperty.setter.call(newInstance, dataValue)
//                }
//            }

//        return newInstance
    }

    fun updateExistingEntity(data: Map<String, Any?>, entity: T): T {
        println("DATA IN UPDATE EXISTING ENTITY: $data")
        val typedData = typeReAttacher.reAttachType(data, entity::class as KClass<T>)
        println("TYPED DATA: $typedData")

        val entityProperties =
            entity::class.memberProperties.filterIsInstance<KMutableProperty<*>>().associateBy { it.name }

        typedData.forEach { (key, value) ->
            val correspondingEntityProperty = entityProperties[key] ?: entityProperties[key.removePrefix("_")]

            if (correspondingEntityProperty == null) {
                throw IllegalArgumentException("Field $key does not exist in the entity.")
            }

            correspondingEntityProperty.isAccessible = true

            when {
                value == null -> {
                    if (!correspondingEntityProperty.returnType.isMarkedNullable) {
                        throw IllegalArgumentException("Field $key cannot be set to null.")
                    } else {
                        correspondingEntityProperty.setter.call(entity, null)
                    }
                }

                key == AugmentableBaseEntity::pseudoProperties.name -> {
                    if (entity is AugmentableBaseEntity) {
                        validateDataAsPseudoProperties(entity, value)
                        val existingPseudoProperties = entity.pseudoProperties
                        val mergedPseudoProperties =
                            mergePseudoProperties(existingPseudoProperties, value as Map<String, Any?>)
                        correspondingEntityProperty.setter.call(entity, mergedPseudoProperties)
                    } else {
                        throw IllegalArgumentException("Entity does not support pseudoProperties")
                    }
                }

                key == IPseudoProperty::typeDescriptor.name -> {
                    if (entity is IPseudoProperty) {
                        validateTypeDescriptor(value)
                        val serialized = serialize(value as TypeDescriptor)
                        correspondingEntityProperty.setter.call(entity, serialized)
                    } else {
                        throw IllegalArgumentException("Entity does not support typeDescriptor")
                    }
                }

                value::class.createType() != correspondingEntityProperty.returnType -> {
                    throw IllegalArgumentException(
                        "Field $key must be of type ${correspondingEntityProperty.returnType}."
                    )
                }

                else -> {
                    correspondingEntityProperty.setter.call(entity, value)
                }
            }
        }

        return entity
    }

    private fun validateTypeDescriptor(value: Any?) {
        if (value == null) throw IllegalArgumentException("TypeDescriptor must be provided")

        if (value !is TypeDescriptor) throw IllegalArgumentException("TypeDescriptor must be a TypeDescriptor")
    }

    private fun validateDataAsPseudoProperties(
        updatedEntity: AugmentableBaseEntity, data: Any?
    ) {
        if (data !is Map<*, *>) throw IllegalArgumentException("PseudoProperties must be a Map")

        val asTypesafeMap = data.entries.associate { (key, value) ->
            if (key !is String) throw IllegalArgumentException("PseudoProperty-keys must be Strings")
            key to value
        }

        val validPseudoProperties = getValidPseudoProperties(updatedEntity)

        val requiredPseudoProperties = validPseudoProperties.filter {
            when (val typeDescriptor = it.typeDescriptor) {
                is TypeDescriptor.CollectionDescriptor, is TypeDescriptor.MapDescriptor -> typeDescriptor.hasMinElementsOrEntries()

                else -> !typeDescriptor.isNullable()
            }
        }

        val missingPseudoProperties = requiredPseudoProperties.filterNot {
            asTypesafeMap.containsKey(it.key)
        }.filterNot { requiredProperty ->
            updatedEntity.pseudoProperties.containsKey(requiredProperty.key) || asTypesafeMap.containsKey(
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
            } else registeredPseudoProperty.let {
                val typeDescriptor = it.typeDescriptor
                val failureDetails = mutableListOf<String>()
                val isValid = try {
                    ValueType.validateValueAgainstDescriptor(typeDescriptor, value, failureDetails)
                } catch (e: Exception) {
                    failureDetails.add("Validation error for pseudo-property '$key': ${e.message}")
                    false
                }

                if (!isValid) {
                    "Pseudo-property '$key' does not match the expected type or constraints. Descriptor: " + "$typeDescriptor,\n\n Value: $value.\n" + "\n Details: ${
                        failureDetails.joinToString(
                            "; "
                        )
                    }"
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

    private fun mergePseudoProperties(existing: Map<String, Any?>, updates: Map<String, Any?>) =
        serialize(existing + updates)


    private fun getValidPseudoProperties(entity: AugmentableBaseEntity): List<IPseudoProperty> {
        return _pseudoPropertyRepository.findAllByEntitySimpleName(entity::class.simpleName!!)
    }

    private fun serialize(value: Any): String {
        return objectMapper.writeValueAsString(value)
    }
}

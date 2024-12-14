package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.IPseudoProperty
import com.ecommercedemo.common.persistence.concretion._pseudoProperty._PseudoPropertyRepository
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@Service
@Suppress("UNCHECKED_CAST")
class ServiceUtility<T : BaseEntity>(
    private val _pseudoPropertyRepository: _PseudoPropertyRepository,
) {

    fun createNewInstance(
        instanceClass: KClass<T>,
        data: Map<String, Any?>,
    ): T {
        if (instanceClass is AugmentableBaseEntity)
            validatePseudoProperties(instanceClass as AugmentableBaseEntity, data)

        val entityConstructor = instanceClass.constructors.find { it.parameters.isNotEmpty() }
            ?: throw IllegalArgumentException("No suitable constructor found for ${instanceClass.simpleName}")

        val instanceConstructorParams = entityConstructor.parameters
            .filter { it.name in data.keys }
            .associateWith { param ->
                println("PARAM: ${param.name}")
                val value = data[param.name?.removePrefix("_")]
                println("VALUE: $value")

                when {
                    param.name == AugmentableBaseEntity::pseudoProperties.name
                            && data[AugmentableBaseEntity::pseudoProperties.name] != null -> {
                        if (instanceClass is AugmentableBaseEntity) {
                            instanceClass.pseudoProperties + value as Map<String, Any?>
                        } else throw IllegalArgumentException("Entity does not support pseudoProperties")
                    }

                    param.name == IPseudoProperty::typeDescriptor.name
                            && data[IPseudoProperty::typeDescriptor.name] != null -> {
                        if (instanceClass.isSubclassOf(IPseudoProperty::class)) {
                            validateTypeDescriptor(data[IPseudoProperty::typeDescriptor.name])
                            value
                        } else throw IllegalArgumentException("Entity does not support typeDescriptor")
                    }

                    value != null -> value

                    param.type.isMarkedNullable || param.isOptional -> null

                    else -> throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
                }
            }
        return entityConstructor.callBy(instanceConstructorParams).apply {
            val memberProperties = this::class.memberProperties
            val privateFields = memberProperties
                .filter { it.name.startsWith("_") }
            val correspondingPublicFieldNames = privateFields.map { it.name.removePrefix("_") }
            memberProperties
                .filter { it.name in correspondingPublicFieldNames }
                .filterIsInstance<KMutableProperty<*>>()
                .onEach { it.isAccessible = true }
                .forEach { it.setter.call(this, data[it.name]) }
        }
    }


    fun updateExistingEntity(data: Map<String, Any?>, entity: T): T {
        val entityProperties =
            entity::class.memberProperties.filterIsInstance<KMutableProperty<*>>().associateBy { it.name }

        data.forEach { (key, value) ->
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
                        validatePseudoProperties(entity, data)
                        println("Validation passed")
                        val existingPseudoProperties = entity.pseudoProperties
                        println("EXISTING PSEUDO PROPERTIES: $existingPseudoProperties")
                        val mergedPseudoProperties = existingPseudoProperties + value as Map<String, Any?>
                        println("MERGED PSEUDO PROPERTIES: $mergedPseudoProperties")
                        correspondingEntityProperty.setter.call(entity, mergedPseudoProperties)
                    } else {
                        throw IllegalArgumentException("Entity does not support pseudoProperties")
                    }
                }

                key == IPseudoProperty::typeDescriptor.name -> {
                    if (entity is IPseudoProperty) {
                        validateTypeDescriptor(value)
                        correspondingEntityProperty.setter.call(entity, value)
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

    private fun validatePseudoProperties(
        entity: AugmentableBaseEntity, data: Map<String, Any?>
    ) {
        val validPseudoProperties = getValidPseudoProperties(entity)
        val requiredPseudoProperties = validPseudoProperties.filter {
            when (val typeDescriptor = it.typeDescriptor) {
                is TypeDescriptor.CollectionDescriptor, is TypeDescriptor.MapDescriptor -> typeDescriptor.hasMinElementsOrEntries()

                else -> !typeDescriptor.isNullable()
            }
        }

        if (requiredPseudoProperties.isEmpty() && !data.containsKey(AugmentableBaseEntity::pseudoProperties.name)) return
        if (data[AugmentableBaseEntity::pseudoProperties.name] !is Map<*, *>)
            throw IllegalArgumentException("PseudoProperties must be a Map")

        val pseudoProperties = data[AugmentableBaseEntity::pseudoProperties.name] as Map<String, Any?>


        val missingPseudoProperties = requiredPseudoProperties.filterNot {
            pseudoProperties.containsKey(it.key)
        }

        if (missingPseudoProperties.isNotEmpty()) {
            throw IllegalArgumentException(
                "Missing required pseudo-properties: ${missingPseudoProperties.joinToString(", ") { it.key }}"
            )
        }

        val validationErrors = pseudoProperties.mapNotNull { (key, value) ->
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
                    "Pseudo-property '$key' does not match the expected type or constraints. Descriptor: $typeDescriptor,\n\n Value: $value.\n" + "\n Details: ${
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


    private fun getValidPseudoProperties(entity: AugmentableBaseEntity): List<IPseudoProperty> {
        return _pseudoPropertyRepository.findAllByEntitySimpleName(entity::class.simpleName!!)
    }


}

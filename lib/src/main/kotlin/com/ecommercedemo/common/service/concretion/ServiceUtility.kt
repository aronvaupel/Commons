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
        when {
            instanceClass.isSubclassOf(AugmentableBaseEntity::class) ->
                validatePseudoProperties(instanceClass as KClass<out AugmentableBaseEntity>, data)

            !(instanceClass.isSubclassOf(AugmentableBaseEntity::class))
                    && data.containsKey(AugmentableBaseEntity::pseudoProperties.name) ->
                throw IllegalArgumentException("Entity does not support pseudoProperties")

            instanceClass.isSubclassOf(IPseudoProperty::class) ->
                validateTypeDescriptor(data[IPseudoProperty::typeDescriptor.name])
        }

        val entityConstructor = instanceClass.constructors.find { it.parameters.isNotEmpty() }
            ?: throw IllegalArgumentException("No suitable constructor found for ${instanceClass.simpleName}")

        val instanceConstructorParams = entityConstructor.parameters
            .filter { it.name in data.keys }
            .associateWith { param ->
                val value = data[param.name?.removePrefix("_")]

                when {
                    value != null -> {
                        println("VALUE: $value")
                        value
                    }

                    param.type.isMarkedNullable || param.isOptional -> null
                    else -> throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
                }
            }

        val memberProperties = instanceClass::class.memberProperties
        println("MEMBER PROPERTIES: $memberProperties")

        val privateFields = memberProperties
            .filter { it.name.startsWith("_") }
        println("PRIVATE FIELDS: $privateFields")

        val correspondingPublicFieldNames = privateFields.map { it.name.removePrefix("_") }
        println("CORRESPONDING PUBLIC FIELD NAMES: $correspondingPublicFieldNames")

        val otherFields = data.filter {
            !(it.key.startsWith("_")) && it.key !in instanceConstructorParams.keys.map { param -> param.name }
        }.map { it.key }
        println("OTHER FIELDS: $otherFields")

        val allAdditionalFieldNames = correspondingPublicFieldNames + otherFields
        println("ALL ADDITIONAL FIELD NAMES: $allAdditionalFieldNames")

        return entityConstructor.callBy(instanceConstructorParams).apply {
            memberProperties
                .filter { it.name in allAdditionalFieldNames }
                .filterIsInstance<KMutableProperty<*>>()
                .onEach { it.isAccessible = true }
                .forEach { it.setter.call(this, data[it.name]) }

        }
    }

    fun updateExistingEntity(data: Map<String, Any?>, entity: T): T {
        if (entity is AugmentableBaseEntity) {
            validatePseudoProperties(entity::class as KClass<out AugmentableBaseEntity>, data)
        }

        val entityProperties =
            entity::class.memberProperties.filterIsInstance<KMutableProperty<*>>().associateBy { it.name }

        data.forEach { (key, value) ->
            val correspondingEntityProperty = entityProperties[key.removePrefix("_")]
                ?: throw IllegalArgumentException("Field $key does not exist in the entity.")

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
                        val existingPseudoProperties = entity.pseudoProperties

                        val mergedPseudoProperties = existingPseudoProperties + value as Map<String, Any?>

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
        entity: KClass<out AugmentableBaseEntity>, data: Map<String, Any?>, isUpdate: Boolean = false
    ) {
        val validPseudoProperties = getValidPseudoProperties(entity)
        println("VALID PSEUDO PROPERTIES: $validPseudoProperties")

        val pseudoProperties = data[AugmentableBaseEntity::pseudoProperties.name]

        if (pseudoProperties !is Map<*, *>)
            throw IllegalArgumentException("PseudoProperties must be a Map")


        if (!isUpdate) {
            val requiredPseudoProperties = validPseudoProperties.filter {
                when (val typeDescriptor = it.typeDescriptor) {
                    is TypeDescriptor.CollectionDescriptor, is TypeDescriptor.MapDescriptor -> typeDescriptor.hasMinElementsOrEntries()

                    else -> !typeDescriptor.isNullable()
                }
            }
            println("REQUIRED PSEUDO PROPERTIES: $requiredPseudoProperties")
            when {
                requiredPseudoProperties.isEmpty() && !data.containsKey(AugmentableBaseEntity::pseudoProperties.name) -> return
                requiredPseudoProperties.isNotEmpty() && !data.containsKey(AugmentableBaseEntity::pseudoProperties.name) ->
                    throw IllegalArgumentException("PseudoProperties must be provided")
            }
            val missingPseudoProperties = requiredPseudoProperties.filterNot {
                pseudoProperties.containsKey(it.key)
            }
            println("MISSING PSEUDO PROPERTIES: $missingPseudoProperties")

            if (missingPseudoProperties.isNotEmpty()) {
                throw IllegalArgumentException(
                    "Missing required pseudo-properties: ${missingPseudoProperties.joinToString(", ") { it.key }}"
                )
            }
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


    private fun getValidPseudoProperties(entityClass: KClass<out AugmentableBaseEntity>): List<IPseudoProperty> {
        return _pseudoPropertyRepository.findAllByEntitySimpleName(entityClass.simpleName!!)
    }


}

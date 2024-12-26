package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.IPseudoProperty
import com.ecommercedemo.common.persistence.concretion._pseudoProperty._PseudoPropertyRepository
import com.ecommercedemo.common.service.CachingEligible
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible

@Service
@Suppress("UNCHECKED_CAST")
class ServiceUtility<T : BaseEntity>(
    private val _pseudoPropertyRepository: _PseudoPropertyRepository,
    private val reflectionService: ReflectionService,
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
        println("When-blocked passed")

        val entityConstructor = reflectionService.findConstructorWithArgs(instanceClass)
        println("entityConstructor: $entityConstructor")

        val instanceConstructorParams = createConstructorParams(entityConstructor, data)

        return entityConstructor.callBy(instanceConstructorParams).apply {
            val memberProperties = reflectionService.getClassMemberProperties(instanceClass)
            println("memberProperties: $memberProperties")
            val remainingFields = data.filter {
                !(it.key.startsWith("_")) && it.key !in instanceConstructorParams.keys.map { param -> param.name }
            }.map { it.key }
            memberProperties?.filter { it.name in remainingFields }?.filterIsInstance<KMutableProperty<*>>()
                ?.onEach { it.isAccessible = true }?.forEach { it.setter.call(this, data[it.name]) }
        }
    }

    @CachingEligible
    private fun createConstructorParams(
        entityConstructor: KFunction<T>,
        data: Map<String, Any?>
    ): Map<KParameter, Any?> {
        val instanceConstructorParams = reflectionService.getConstructorParams(entityConstructor)
            .filter { it.name in data.keys }
            .associateWith { param ->
                val value = data[param.name?.removePrefix("_")]

                when {
                    value != null -> {
                        value
                    }

                    param.type.isMarkedNullable || param.isOptional -> null
                    else -> throw IllegalArgumentException("Field ${param.name} must be provided and cannot be null.")
                }
            }
        println("instanceConstructorParams: $instanceConstructorParams")
        return instanceConstructorParams
    }

    fun updateExistingEntity(data: Map<String, Any?>, entity: T): T {
        if (entity is AugmentableBaseEntity) {
            validatePseudoProperties(entity::class as KClass<out AugmentableBaseEntity>, data)
        }

        val entityProperties =
            reflectionService.findMutableProperties(entity)

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


    @CachingEligible
    private fun validateTypeDescriptor(value: Any?) {
        if (value == null) throw IllegalArgumentException("TypeDescriptor must be provided")
        if (value !is TypeDescriptor) throw IllegalArgumentException("TypeDescriptor must be a TypeDescriptor")
    }

    @CachingEligible
    private fun validatePseudoProperties(
        entity: KClass<out AugmentableBaseEntity>, data: Map<String, Any?>, isUpdate: Boolean = false
    ) {
        val validPseudoProperties = getValidPseudoProperties(entity)
        val pseudoProperties = data[AugmentableBaseEntity::pseudoProperties.name]
        if (validPseudoProperties.isEmpty() && pseudoProperties == null) return

        if (pseudoProperties !is Map<*, *>)
                throw IllegalArgumentException("PseudoProperties must be a Map")


        if (!isUpdate) {
            val requiredPseudoProperties = validPseudoProperties.filter {
                when (val typeDescriptor = it.typeDescriptor) {
                    is TypeDescriptor.CollectionDescriptor, is TypeDescriptor.MapDescriptor -> typeDescriptor.hasMinElementsOrEntries()

                    else -> !typeDescriptor.isNullable()
                }
            }

            when {
                requiredPseudoProperties.isEmpty() && !data.containsKey(AugmentableBaseEntity::pseudoProperties.name) -> return
                requiredPseudoProperties.isNotEmpty() && !data.containsKey(AugmentableBaseEntity::pseudoProperties.name) ->
                    throw IllegalArgumentException("PseudoProperties must be provided")
            }
            val missingPseudoProperties = requiredPseudoProperties.filterNot {
                pseudoProperties.containsKey(it.key)
            }

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

    @CachingEligible
    private fun getValidPseudoProperties(entityClass: KClass<out AugmentableBaseEntity>): List<IPseudoProperty> {
        return _pseudoPropertyRepository.findAllByEntitySimpleName(entityClass.simpleName!!)
    }


}

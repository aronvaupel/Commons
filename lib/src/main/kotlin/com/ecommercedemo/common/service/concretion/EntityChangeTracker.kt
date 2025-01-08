package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import org.springframework.stereotype.Service
import kotlin.reflect.jvm.isAccessible

@Service
@Suppress("UNCHECKED_CAST")
class EntityChangeTracker<T : BaseEntity>(
    private val reflectionService: ReflectionService,
    private val objectMapper: ObjectMapper
) {

//    fun getChangedProperties(entityBefore: T?, entityAfter: T): MutableMap<String, Any?> {
//        val changedProperties = mutableMapOf<String, Any?>()
//        val propertiesBefore =
//            reflectionService.getEntityMemberProperties(entityBefore)
//        reflectionService.getEntityMemberProperties(entityAfter)?.forEach { property ->
//            val oldValue = entityBefore?.let { before ->
//                val matchingProperty = propertiesBefore?.firstOrNull { it.name == property.name }
//                    ?: throw IllegalArgumentException("Property ${property.name} not found in entity before.")
//                matchingProperty.isAccessible = true
//                if (before is AugmentableBaseEntity && property.name == AugmentableBaseEntity::pseudoProperties.name)
//                    before.pseudoProperties
//                else matchingProperty.get(before)
//            }
//
//            val newValue = if (
//                entityAfter is AugmentableBaseEntity
//                && property.name == AugmentableBaseEntity::pseudoProperties.name
//            )
//                entityAfter.pseudoProperties
//            else {
//                property.isAccessible = true
//                property.get(entityAfter)
//            }
//
//            if (oldValue != newValue) {
//                changedProperties[property.name] = if (property.name == AugmentableBaseEntity::pseudoProperties.name)
//                    ObjectMapper().writeValueAsString(newValue)
//                else
//                    newValue
//            }
//
//        }
//        println("Changed properties: $changedProperties")
//        return changedProperties
//    }

    fun getChangedProperties(entityBefore: T?, entityAfter: T): MutableMap<String, Any?> {
        val changedProperties = mutableMapOf<String, Any?>()
        val propertiesBefore = reflectionService.getEntityMemberProperties(entityBefore)
        val memberProperties = reflectionService.getEntityMemberProperties(entityAfter)

        memberProperties?.forEach { property ->
            val oldValue = entityBefore?.let { before ->
                val matchingProperty = propertiesBefore?.firstOrNull { it.name == property.name }
                    ?: throw IllegalArgumentException(
                        "Property ${property.name} not found in entityBefore of type ${entityBefore::class.simpleName}."
                    )
                matchingProperty.isAccessible = true
                matchingProperty.get(before)
            }

            val newValue = property.run {
                isAccessible = true
                get(entityAfter)
            }


            val isEqual = when {
                oldValue == null || newValue == null -> oldValue == newValue
                isComplexObject(oldValue) ->
                    objectMapper.writeValueAsString(oldValue) == objectMapper.writeValueAsString(newValue)

                isMap(oldValue) ->
                    objectMapper.writeValueAsString(oldValue) == objectMapper.writeValueAsString(newValue)

                isCollection(oldValue) ->
                    objectMapper.writeValueAsString(oldValue) == objectMapper.writeValueAsString(newValue)

                else -> oldValue == newValue
            }

            if (!isEqual) when {
                isComplexObject(newValue) ->
                    changedProperties[property.name] = objectMapper.writeValueAsString(newValue)

                isMap(newValue) ->
                    changedProperties[property.name] = objectMapper.writeValueAsString(newValue)

                isCollection(newValue) ->
                    changedProperties[property.name] = objectMapper.writeValueAsString(newValue)

                property.name == AugmentableBaseEntity::pseudoProperties.name ->
                    objectMapper.writeValueAsString(newValue)

                else -> changedProperties[property.name] = newValue

            }
        }
        println("Changed properties: $changedProperties")
        return changedProperties
    }

    private fun isComplexObject(value: Any?) =
        value != null && (value::class.annotations.any { it is Entity || it is Embeddable } || value::class.isData)

    private fun isCollection(value: Any?): Boolean = value != null && value is Collection<*>
    private fun isMap(value: Any?): Boolean = value != null && value is Map<*, *>

}

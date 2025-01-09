package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import org.springframework.stereotype.Service
import kotlin.reflect.jvm.isAccessible

@Service
class EntityChangeTracker<T : BaseEntity>(
    private val reflectionService: ReflectionService,
    private val objectMapper: ObjectMapper
) {
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
                isComplexObject(oldValue) || isMap(oldValue) || isCollection(oldValue) ->
                    objectMapper.writeValueAsString(oldValue) == objectMapper.writeValueAsString(newValue)

                else -> oldValue == newValue
            }

            if (!isEqual) changedProperties[property.name] = when {
                isComplexObject(newValue) ->
                    objectMapper.convertValue(newValue, object : TypeReference<Map<String, Any?>>() {})

                isCollection(newValue) -> objectMapper.convertValue(newValue, object : TypeReference<List<Any?>>() {})
                else -> newValue
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

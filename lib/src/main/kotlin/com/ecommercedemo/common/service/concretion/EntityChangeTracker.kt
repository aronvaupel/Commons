package com.ecommercedemo.common.service.concretion

import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Service
import kotlin.reflect.jvm.isAccessible

@Service
@DependsOn("reflectionService")
class EntityChangeTracker<T : BaseEntity>(
    private val reflectionService: ReflectionService,
) {

    fun getChangedProperties(entityBefore: T?, entityAfter: T): MutableMap<String, Any?> {
        val changedProperties = mutableMapOf<String, Any?>()
        val propertiesBefore =
            reflectionService.getMemberProperties(entityBefore)
        reflectionService.getMemberProperties(entityAfter)?.forEach { property ->
            val oldValue = entityBefore?.let { before ->
                val matchingProperty = propertiesBefore?.firstOrNull { it.name == property.name }
                    ?: throw IllegalArgumentException("Property ${property.name} not found in entity before.")
                matchingProperty.isAccessible = true
                if (before is AugmentableBaseEntity && property.name == AugmentableBaseEntity::pseudoProperties.name)
                    before.pseudoProperties
                else matchingProperty.get(before)
            }

            val newValue = if (
                entityAfter is AugmentableBaseEntity
                && property.name == AugmentableBaseEntity::pseudoProperties.name
            )
                entityAfter.pseudoProperties
            else {
                property.isAccessible = true
                property.get(entityAfter)
            }

            if (oldValue != newValue) {
                changedProperties[property.name] = if (property.name == AugmentableBaseEntity::pseudoProperties.name)
                    ObjectMapper().writeValueAsString(newValue)
                else
                    newValue
            }

        }
        return changedProperties
    }

}
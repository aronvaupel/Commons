package com.ecommercedemo.common.application

import com.ecommercedemo.common.model.abstraction.AugmentableBaseEntity
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.service.concretion.ServiceUtility
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class EntityChangeTracker<T : BaseEntity>(
    private val serviceUtility: ServiceUtility,
) {

    fun getChangedProperties(entityBefore: T?, entityAfter: T): MutableMap<String, Any?> {
        val changedProperties = mutableMapOf<String, Any?>()
        val propertiesBefore =
            entityBefore?.let { entityBefore::class.memberProperties.filterIsInstance<KProperty1<T, *>>() }
        val propertiesAfter = entityAfter::class.memberProperties.filterIsInstance<KProperty1<T, *>>()
        propertiesAfter.forEach { property ->
            val oldValue = entityBefore?.let { beforeEntity ->
                val matchingProperty = propertiesBefore?.firstOrNull { it.name == property.name }
                    ?: throw IllegalArgumentException("Property ${property.name} not found in entity before.")
                matchingProperty.isAccessible = true
                if (property.name == AugmentableBaseEntity::pseudoProperties.name)
                    serviceUtility.deserializePseudoProperty(matchingProperty.get(beforeEntity) as String)
                else matchingProperty.get(beforeEntity)
            }

            val newValue = if (property.name == AugmentableBaseEntity::pseudoProperties.name)
                serviceUtility.deserializePseudoProperty(property.get(entityAfter) as String)
            else property.get(entityAfter)

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
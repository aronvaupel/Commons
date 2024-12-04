package com.ecommercedemo.common.application

import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.ExpandableBaseEntity
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
        val properties = entityAfter::class.memberProperties.filterIsInstance<KProperty1<T, *>>()
        println("Getting changed properties for entity of type ${entityAfter::class.simpleName}, properties: $properties")
        properties.forEach { property ->
            property.isAccessible = true
            val oldValue = entityBefore?.let {
                if (property.name == ExpandableBaseEntity::pseudoProperties.name)
                    serviceUtility.deserializeJsonBProperty(property.get(it) as String)
                else property.get(it)
            }

            val newValue = if (property.name == ExpandableBaseEntity::pseudoProperties.name)
                serviceUtility.deserializeJsonBProperty(property.get(entityAfter) as String)
            else property.get(entityAfter)

            if (oldValue != newValue) {
                changedProperties[property.name] = if (property.name == ExpandableBaseEntity::pseudoProperties.name)
                    ObjectMapper().writeValueAsString(newValue)
                else
                    newValue
            }

        }
        return changedProperties
    }
}
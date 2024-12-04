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
        println("EntityChangeTracker: ORIGINAL ENTITY: $entityBefore with properties: ${entityBefore?.let { it::class.memberProperties.filterIsInstance<KProperty1<T, *>>() }}")
        val changedProperties = mutableMapOf<String, Any?>()
        val propertiesAfter = entityAfter::class.memberProperties.filterIsInstance<KProperty1<T, *>>()
        println("EntityChangeTracker: Getting changed properties for ENTITY AFTER of type ${entityAfter::class.simpleName}, properties: $propertiesAfter")
        propertiesAfter.forEach { property ->
            println("EntityChangeTracker: Checking property in updated entity: ${property.name}")
            property.isAccessible = true
            val oldValue = entityBefore?.let {
                if (property.name == ExpandableBaseEntity::pseudoProperties.name)
                    serviceUtility.deserializeJsonBProperty(property.get(it) as String)
                else property.get(it)
            }
            println("EntityChangeTracker: OLD VALUE: $oldValue")

            val newValue = if (property.name == ExpandableBaseEntity::pseudoProperties.name)
                serviceUtility.deserializeJsonBProperty(property.get(entityAfter) as String)
            else property.get(entityAfter)
            println("EntityChangeTracker: NEW VALUE: $newValue")

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
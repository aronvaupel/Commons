package com.ecommercedemo.common.application

import com.ecommercedemo.common.model.abstraction.BaseEntity
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class EntityChangeTracker<T : BaseEntity> {

    fun getChangedProperties(entityBefore: T?, entityAfter: T): MutableMap<String, Any?> {
        val changedProperties = mutableMapOf<String, Any?>()
        val properties = entityAfter::class.memberProperties.filterIsInstance<KProperty1<T, *>>()
        println("Getting changed properties for entity of type ${entityAfter::class.simpleName}, properties: $properties")
        properties.forEach { property ->
            property.isAccessible = true
            val oldValue = entityBefore?.let { property.get(it) }
            println("Old value for property ${property.name}: $oldValue")
            val newValue = property.get(entityAfter)
            println("New value for property ${property.name}: $newValue")
            if (oldValue != newValue) {
                changedProperties[property.name] = newValue

            }
        }

        return changedProperties
    }
}
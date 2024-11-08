package com.ecommercedemo.common.util

import com.ecommercedemo.common.model.BaseEntity
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class EntityChangeTracker<T : BaseEntity> {

    fun getChangedProperties(entityBefore: T?, entityAfter: T): MutableMap<String, Any?> {
        val changedProperties = mutableMapOf<String, Any?>()
        val properties = entityAfter::class.memberProperties.filterIsInstance<KProperty1<T, *>>()

        properties.forEach { property ->
            property.isAccessible = true
            val oldValue = entityBefore?.let { property.get(it) }
            val newValue = property.get(entityAfter)

            println("Checking property: ${property.name}, Old Value: $oldValue, New Value: $newValue") // Debug log

            if (oldValue != newValue) {
                changedProperties[property.name] = newValue
                println("Detected change for property: ${property.name}") // Log detected changes

            }
        }

        return changedProperties
    }
}
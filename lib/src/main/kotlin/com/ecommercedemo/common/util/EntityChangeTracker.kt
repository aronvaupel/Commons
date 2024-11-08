package com.ecommercedemo.common.util

import com.ecommercedemo.common.model.BaseEntity
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class EntityChangeTracker<T : BaseEntity> {

    fun getChangedProperties(entityBefore: T?, entityAfter: T): MutableMap<String, Any?> {
        val changedProperties = mutableMapOf<String, Any?>()

        val oldValues = entityBefore?.let { snapshotProperties(it) } ?: emptyMap()
        val newValues = snapshotProperties(entityAfter)

        newValues.forEach { (propertyName, newValue) ->
            val oldValue = oldValues[propertyName]
            println("Checking property: $propertyName, Old Value: $oldValue, New Value: $newValue")

            if (oldValue != newValue) {
                changedProperties[propertyName] = newValue
                println("Detected change for property: $propertyName")
            }
        }

        return changedProperties
    }

    private fun snapshotProperties(entity: T): Map<String, Any?> {
        val properties = entity::class.memberProperties.filterIsInstance<KProperty1<T, *>>()
        return properties.associate { property ->
            property.isAccessible = true
            property.name to property.get(entity)
        }
    }
}
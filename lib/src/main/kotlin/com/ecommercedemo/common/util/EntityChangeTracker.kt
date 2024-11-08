package com.ecommercedemo.common.util

import com.ecommercedemo.common.model.BaseEntity
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

class EntityChangeTracker<T : BaseEntity> {

    fun getChangedProperties(entityBefore: T?, entityAfter: T): MutableMap<String, Any?> {
        val changedProperties = mutableMapOf<String, Any?>()

        // Get properties for both entities
        val newProperties = entityAfter::class.memberProperties.filterIsInstance<KProperty1<T, *>>()
        val oldProperties = entityBefore?.let { it::class.memberProperties.filterIsInstance<KProperty1<T, *>>() } ?: newProperties

        // Compare properties individually
        newProperties.forEach { newProperty ->
            newProperty.isAccessible = true
            val oldProperty = oldProperties.find { it.name == newProperty.name }
            oldProperty?.isAccessible = true

            val oldValue = entityBefore?.let { oldProperty?.get(it) }
            val newValue = newProperty.get(entityAfter)

            // Log and detect changes
            println("Checking property: ${newProperty.name}, Old Value: $oldValue, New Value: $newValue")
            if (oldValue != newValue) {
                changedProperties[newProperty.name] = newValue
                println("Detected change for property: ${newProperty.name}")
            }
        }

        return changedProperties
    }
}
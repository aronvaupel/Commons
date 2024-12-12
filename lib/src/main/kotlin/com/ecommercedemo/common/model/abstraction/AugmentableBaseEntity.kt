package com.ecommercedemo.common.model.abstraction

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Type


@MappedSuperclass
@Suppress("unused")
abstract class AugmentableBaseEntity: BaseEntity() {
    @get:Type(JsonBinaryType::class)
    @get:Column(name = "pseudo_properties", columnDefinition = "jsonb")
    open var pseudoProperties: MutableMap<String, Any?> = mutableMapOf()

    fun getPseudoProperty(key: String): Any? {
        return pseudoProperties[key]
    }

    fun addPseudoProperty(key: String, value: Any?) {
        pseudoProperties = pseudoProperties.toMutableMap().apply {
            this[key] = value
        }
    }

    fun removePseudoProperty(key: String) {
        pseudoProperties = pseudoProperties.toMutableMap().apply {
            this.remove(key)
        }
    }

    fun renamePseudoProperty(oldKey: String, newKey: String) {
        try {
            require(oldKey.isNotBlank()) { "Old key must not be blank." }
            require(newKey.isNotBlank()) { "New key must not be blank." }
            if (oldKey == newKey) {
                println("No renaming needed: 'oldKey' and 'newKey' are the same.")
                return
            }
            if (!pseudoProperties.containsKey(oldKey)) {
                throw IllegalArgumentException("Key '$oldKey' does not exist in pseudoProperties.")
            }

            pseudoProperties = pseudoProperties.toMutableMap().apply {
                this[newKey] = this.remove(oldKey)
            }
            println("Successfully renamed key '$oldKey' to '$newKey'.")
        } catch (e: Exception) {
            println("Failed to rename key: ${e.message}")
            throw e
        }
    }
}

package com.ecommercedemo.common.model.abstraction

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import mu.KotlinLogging
import org.hibernate.annotations.Type


@MappedSuperclass
@Suppress("unused")
abstract class AugmentableBaseEntity: BaseEntity() {
    @Type(JsonType::class)
    @Column(name = "pseudo_properties", columnDefinition = "jsonb")
    open var pseudoProperties: Map<String, Any?> = mapOf()

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
        val log = KotlinLogging.logger {}
        try {
            require(oldKey.isNotBlank()) { "Old key must not be blank." }
            require(newKey.isNotBlank()) { "New key must not be blank." }
            if (oldKey == newKey) {
                return
            }
            pseudoProperties = pseudoProperties.toMutableMap().apply {
                this[newKey] = this.remove(oldKey)
            }
        } catch (e: Exception) {
            log.warn("Failed to rename key: ${e.message}")
            log.debug { e.stackTraceToString() }
            throw e
        }
    }
}

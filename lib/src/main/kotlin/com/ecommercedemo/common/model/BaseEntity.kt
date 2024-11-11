package com.ecommercedemo.common.model

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*


@MappedSuperclass
@Suppress("unused", "JpaQlInspection", "JpaDataSourceORMInspection")
abstract class BaseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    open val id: UUID = UUID.randomUUID()

    @Column(nullable = false, updatable = false)
    open var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    open var updatedAt: LocalDateTime = LocalDateTime.now()

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pseudo_property_data", joinColumns = [JoinColumn(name = "base_entity_id")])
    @MapKeyColumn(name = "key")
    @Column(name = "pseudo_property", columnDefinition = "jsonb")
    open var pseudoProperties: MutableMap<String, Any> = mutableMapOf()

    fun addPseudoProperty(key: String, value: Any, overwrite: Boolean = false) {
        if (!overwrite && pseudoProperties.containsKey(key)) {
            throw IllegalArgumentException("Pseudo-property with key '$key' already exists.")
        }
        pseudoProperties[key] = value
    }

    fun getPseudoProperty(key: String): Any? {
        return pseudoProperties[key]
    }

    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
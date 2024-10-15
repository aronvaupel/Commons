package com.ecommercedemo.common.model

import com.ecommercedemo.common.model.embedded.CustomPropertyData
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass


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

    @ElementCollection
    @CollectionTable(name = "custom_property_data", joinColumns = [JoinColumn(name = "base_entity_id")])
    @Column(name = "custom_property", columnDefinition = "jsonb")
    open lateinit var customProperties: MutableSet<CustomPropertyData>

    fun addCustomProperty(property: CustomPropertyData) {
        customProperties.add(property)
    }

    fun getCustomPropertiesForEntity(entity: KClass<*>): List<CustomPropertyData> {
        return customProperties.filter { it.entity == entity.qualifiedName }
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
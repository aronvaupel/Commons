package com.ecommercedemo.model

import com.ecommercedemo.model.embedded.CustomProperty
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID
import kotlin.reflect.KClass


@MappedSuperclass
@Suppress("unused", "JpaQlInspection", "JpaDataSourceORMInspection")
abstract class BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    val id: UUID = UUID.randomUUID()

    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    @ElementCollection
    @CollectionTable(name = "custom_properties", joinColumns = [JoinColumn(name = "base_entity_id")])
    @Column(name = "custom_property", columnDefinition = "jsonb")
    lateinit var customProperties: MutableSet<CustomProperty>

    fun addCustomProperty(property: CustomProperty) {
        customProperties.add(property)
    }

    fun getCustomPropertiesForEntity(entity: KClass<*>): List<CustomProperty> {
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
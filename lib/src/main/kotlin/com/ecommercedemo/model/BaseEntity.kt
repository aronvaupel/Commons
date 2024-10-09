package com.ecommercedemo.model

import com.ecommercedemo.model.embedded.CustomPropertyData
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*
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
    @CollectionTable(name = "custom_property_data", joinColumns = [JoinColumn(name = "base_entity_id")])
    @Column(name = "custom_property", columnDefinition = "jsonb")
    lateinit var customProperties: MutableSet<CustomPropertyData>

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
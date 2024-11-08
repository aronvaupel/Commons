package com.ecommercedemo.common.model

import com.ecommercedemo.common.model.embedded.PseudoPropertyData
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
    @CollectionTable(name = "pseudo_property_data", joinColumns = [JoinColumn(name = "base_entity_id")])
    @Column(name = "pseudo_property", columnDefinition = "jsonb")
    open var pseudoProperties: MutableSet<PseudoPropertyData> = mutableSetOf()

    fun addPseudoProperty(property: PseudoPropertyData) {
        pseudoProperties.add(property)
    }

    fun getPseudoPropertiesForEntity(entity: KClass<*>): List<PseudoPropertyData> {
        return pseudoProperties.filter { it.entity == entity.qualifiedName }
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
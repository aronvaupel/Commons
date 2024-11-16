package com.ecommercedemo.common.model

import jakarta.persistence.*

@MappedSuperclass
@Suppress("unused", "JpaDataSourceORMInspection")

abstract class ExtendableBaseEntity: BaseEntity() {
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pseudo_property_data", joinColumns = [JoinColumn(name = "base_entity_id")])
    @MapKeyColumn(name = "key")
    @Column(name = "pseudo_property", columnDefinition = "jsonb")
    open var pseudoProperties: MutableMap<String, Any> = mutableMapOf()

    fun getPseudoProperty(key: String): Any? {
        return pseudoProperties[key]
    }
}
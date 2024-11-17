package com.ecommercedemo.common.model

import com.ecommercedemo.common.application.validation.StandardJsonbConverter
import jakarta.persistence.*

@MappedSuperclass
@Suppress("unused", "JpaDataSourceORMInspection")

abstract class ExtendableBaseEntity: BaseEntity() {
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "pseudo_property_data", joinColumns = [JoinColumn(name = "base_entity_id")])
    @MapKeyColumn(name = "key")
    @Column(name = "pseudo_property", columnDefinition = "jsonb")
    open var pseudoProperties: MutableMap<String, String> = mutableMapOf()

    fun getPseudoProperty(key: String): Any? {
        val jsonValue = pseudoProperties[key]
        return jsonValue?.let { StandardJsonbConverter.convertToEntityAttribute(it) }
    }

    fun setPseudoProperty(key: String, value: Any) {
        pseudoProperties[key] = StandardJsonbConverter.convertToDatabaseColumn(value) ?: "{}"
    }
}
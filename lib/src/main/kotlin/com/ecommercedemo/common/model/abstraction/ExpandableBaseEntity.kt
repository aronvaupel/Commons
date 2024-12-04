package com.ecommercedemo.common.model.abstraction

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Type

@MappedSuperclass
@Suppress("unused")
abstract class ExpandableBaseEntity: BaseEntity() {
    @Type(JsonBinaryType::class)
    @Column(name = "pseudo_properties", columnDefinition = "jsonb")
    open var pseudoProperties: String = "{}"

    fun getPseudoProperty(key: String): Any? {
        val map: Map<String, Any?> = ObjectMapper().readValue(pseudoProperties, object : TypeReference<Map<String, Any?>>() {})
        return map[key]
    }

    fun setPseudoProperty(key: String, value: Any) {
        val map: MutableMap<String, Any?> = ObjectMapper().readValue(pseudoProperties, object : TypeReference<MutableMap<String, Any?>>() {})
        map[key] = value
        pseudoProperties = ObjectMapper().writeValueAsString(map)
    }

}
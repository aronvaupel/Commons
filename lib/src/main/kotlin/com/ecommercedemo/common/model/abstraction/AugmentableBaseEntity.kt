package com.ecommercedemo.common.model.abstraction

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Type

@MappedSuperclass
@Suppress("unused")
abstract class AugmentableBaseEntity: BaseEntity() {
    @Type(JsonBinaryType::class)
    @Column(name = "pseudo_properties", columnDefinition = "jsonb")
    open var pseudoProperties: String = "{}"

    fun getPseudoProperty(key: String): Any? {
        val map: Map<String, Any?> = ObjectMapper().readValue(pseudoProperties, object : TypeReference<Map<String, Any?>>() {})
        if (!map.containsKey(key)) {
            throw IllegalArgumentException("Pseudo property with key '$key' not found.")
        }
        return map[key]
    }

    fun setPseudoProperty(key: String, value: Any?) {
        val map: MutableMap<String, Any?> = ObjectMapper().readValue(pseudoProperties, object : TypeReference<MutableMap<String, Any?>>() {})
        map[key] = value
        pseudoProperties = ObjectMapper().writeValueAsString(map)
    }

    fun removePseudoProperty(key: String) {
        val map: MutableMap<String, Any?> = ObjectMapper().readValue(pseudoProperties, object : TypeReference<MutableMap<String, Any?>>() {})
        if (!map.containsKey(key)) {
            throw IllegalArgumentException("Pseudo property with key '$key' not found. Cannot delete.")
        }
        map.remove(key)
        pseudoProperties = ObjectMapper().writeValueAsString(map)
    }

    fun renamePseudoProperty(oldKey: String, newKey: String) {
        val map: MutableMap<String, Any?> = ObjectMapper().readValue(pseudoProperties, object : TypeReference<MutableMap<String, Any?>>() {})
        if (!map.containsKey(oldKey)) {
            throw IllegalArgumentException("Pseudo property with key '$oldKey' not found. Cannot rename.")
        }
        if (map.containsKey(newKey)) {
            throw IllegalArgumentException("Pseudo property with key '$newKey' already exists. Cannot rename.")
        }
        map[newKey] = map.remove(oldKey)
        pseudoProperties = ObjectMapper().writeValueAsString(map)
    }

    fun clearPseudoProperties() {
        pseudoProperties = "{}"
    }

    fun getPseudoProperties(): Map<String, Any?> {
        return ObjectMapper().readValue(pseudoProperties, object : TypeReference<Map<String, Any?>>() {})
    }

}
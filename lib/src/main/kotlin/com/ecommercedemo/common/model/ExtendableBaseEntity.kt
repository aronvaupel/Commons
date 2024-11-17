package com.ecommercedemo.common.model

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Type

@MappedSuperclass
@Suppress("unused")

abstract class ExtendableBaseEntity: BaseEntity() {
    @Type(JsonBinaryType::class)
    @Column(name = "pseudo_property", columnDefinition = "jsonb")
    open lateinit var pseudoProperties: String
}
package com.ecommercedemo.common.model.abstraction

import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.fasterxml.jackson.databind.ObjectMapper
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Type

@MappedSuperclass
abstract class BasePseudoProperty: BaseEntity() {
    open val entitySimpleName: String = ""
    open var key: String = ""
    @Type(JsonBinaryType::class)
    @Column(name = "type_descriptor", columnDefinition = "jsonb")
    open var typeDescriptor: String = ""

    fun getTypeDescriptor(): TypeDescriptor {
        return ObjectMapper().readValue(typeDescriptor, TypeDescriptor::class.java)
    }

    fun setTypeDescriptor(typeDescriptor: TypeDescriptor) {
        this.typeDescriptor = ObjectMapper().writeValueAsString(typeDescriptor)
    }

}
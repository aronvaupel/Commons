package com.ecommercedemo.common.model.abstraction

import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Type

@MappedSuperclass
abstract class BasePseudoProperty: BaseEntity() {
    open val entitySimpleName: String = ""
    open var key: String = ""
    @get:Type(JsonBinaryType::class)
    @get:Column(name = "type_descriptor", columnDefinition = "jsonb")
    abstract var typeDescriptor: TypeDescriptor

//    fun getTypeDescriptorDeserialized(): TypeDescriptor {
//        return ObjectMapper().readValue(typeDescriptor, TypeDescriptor::class.java)
//    }
}
package com.ecommercedemo.common.model.abstraction

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.Type

@MappedSuperclass
abstract class BasePseudoProperty : BaseEntity() {

    open val entitySimpleName: String = "DUMMY_CLASS"

    open var key: String = "DUMMY_KEY"

    @get:Type(JsonBinaryType::class)
    @get:Column(name = "type_descriptor", columnDefinition = "jsonb")
    open var typeDescriptor: TypeDescriptor = TypeDescriptor.PrimitiveDescriptor(
        category = "PRIMITIVE",
        type = ValueType.STRING,
        isNullable = true
    )
}
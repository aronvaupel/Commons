package com.ecommercedemo.common.model.abstraction

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type

@MappedSuperclass
abstract class BasePseudoProperty : BaseEntity() {
    @NotNull
    @NotBlank
    @Column(name = "entity_simple_name")
    open var entitySimpleName: String = ""
    @NotBlank
    @NotNull
    @Column(name = "key")
    open var key: String = ""
    @NotNull
    @Type(JsonType::class)
    @Column(name = "type_descriptor", columnDefinition = "jsonb")
    open var typeDescriptor: TypeDescriptor = TypeDescriptor.PrimitiveDescriptor(
        category = "PRIMITIVE",
        type = ValueType.STRING,
        isNullable = true
    )
}
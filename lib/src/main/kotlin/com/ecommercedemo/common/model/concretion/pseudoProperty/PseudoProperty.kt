package com.ecommercedemo.common.model.concretion.pseudoProperty

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.IPseudoProperty
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import org.hibernate.annotations.Type

@Suppress("unused")
@Entity
@Table(name = PseudoProperty.STORAGE_NAME)
open class PseudoProperty(
    @NotBlank
    @Column(name = "entity_simple_name", nullable = false)
    override var entitySimpleName: String = "",

    @NotBlank
    @Column(name = "key", nullable = false)
    override var key: String = "",

    @Type(JsonType::class)
    @Column(
        name = "type_descriptor",
        columnDefinition = "jsonb",
        nullable = false
    )
    override var typeDescriptor: TypeDescriptor = TypeDescriptor.PrimitiveDescriptor(
        category = "PRIMITIVE",
        type = ValueType.STRING,
        isNullable = true
    )
) : BaseEntity(), IPseudoProperty {

    companion object {
        const val STORAGE_NAME = "pseudo_properties"
    }
}

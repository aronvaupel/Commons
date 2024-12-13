package com.ecommercedemo.common.model.concretion.pseudoProperty

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.BaseEntity
import com.ecommercedemo.common.model.abstraction.IPseudoProperty
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type

//Todo: Expiration
@Suppress("unused")
@Entity
@Table(name = PseudoProperty.STORAGE_NAME)
open class PseudoProperty(
    @Column(name = "entity_simple_name", nullable = false)
    override var entitySimpleName: String = "",

    @Column(name = "key", nullable = false)
    override var key: String = "",

    @NotNull
    @Type(JsonType::class)
    @Column(
        name = "type_descriptor",
        columnDefinition = "jsonb"
    ) override var typeDescriptor: TypeDescriptor = TypeDescriptor.PrimitiveDescriptor(
        category = "PRIMITIVE",
        type = ValueType.STRING,
        isNullable = true
    )
) : BaseEntity(), IPseudoProperty {

    companion object {
        const val STORAGE_NAME = "pseudo_properties"
    }
}

package com.ecommercedemo.common.model.concretion._pseudoProperty

import com.ecommercedemo.common.application.validation.type.TypeCategory
import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.ObjectMapper
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type
import java.util.*

@Suppress("unused", "ClassName")
@Entity
@Table(name = _PseudoProperty.STORAGE_NAME)
open class _PseudoProperty(
    override var id: UUID = UUID.randomUUID(),
    @NotNull
    @NotBlank
    @Column(updatable = false)
    override var entitySimpleName: String,
    @NotNull
    @NotBlank
    override var key: String = "",
    @Type(JsonBinaryType::class)
    @Column(columnDefinition = "jsonb", updatable = false)
    override var typeDescriptor: String = ""
) : BasePseudoProperty() {

    @JsonCreator
    constructor() : this(
        UUID.randomUUID(),
        "DUMMY_CLASS",
        "DUMMY_KEY",
        ObjectMapper().writeValueAsString(
            TypeDescriptor.PrimitiveDescriptor(
                category = TypeCategory.PRIMITIVE.name,
                type = ValueType.STRING,
                isNullable = true
            )
        )
    )

    companion object {
        const val STORAGE_NAME = "_pseudo_properties"
    }

}
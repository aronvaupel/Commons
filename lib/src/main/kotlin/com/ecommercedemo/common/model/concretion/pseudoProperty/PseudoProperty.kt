package com.ecommercedemo.common.model.concretion.pseudoProperty

import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.controller.abstraction.util.TypeDescriptor
import com.ecommercedemo.common.model.abstraction.BasePseudoProperty
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type
import java.util.*

//Todo: Expiration
@Suppress("unused")
@Entity
@Table(name = PseudoProperty.STORAGE_NAME)
open class PseudoProperty(
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
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    override var typeDescriptor: TypeDescriptor
) : BasePseudoProperty() {
    @JsonCreator
    constructor() : this(
        UUID.randomUUID(),
        "DUMMY_CLASS",
        "DUMMY_KEY",
        TypeDescriptor.PrimitiveDescriptor(
            type = ValueType.STRING,
            isNullable = true
        )
    )

    companion object {
        const val STORAGE_NAME = "pseudo_properties"
    }

}

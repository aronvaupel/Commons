package com.ecommercedemo.common.model

import com.ecommercedemo.common.application.JsonbConverterForTypeDescriptor
import com.ecommercedemo.common.application.search.TypeDescriptor
import com.ecommercedemo.common.application.validation.type.ValueType
import com.ecommercedemo.common.model.dto.PseudoPropertyDto
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*


@Suppress("unused")
@Entity
@Table(name = PseudoProperty.STORAGE_NAME)
open class PseudoProperty(
    override val id: UUID = UUID.randomUUID(),
    @NotNull
    @NotBlank
    open val entitySimpleName: String,
    @NotNull
    @NotBlank
    open var key: String = "",
    @Convert(converter = JsonbConverterForTypeDescriptor::class)
    @Column(columnDefinition = "jsonb")
    open var typeDescriptor: TypeDescriptor
) : BaseEntity() {
    constructor() : this(UUID.randomUUID(), "DUMMY_CLASS", "DUMMY_KEY", TypeDescriptor.PrimitiveDescriptor(ValueType.ANY))

    fun copy(
        id: UUID = this.id,
        entityClassName: String = this.entitySimpleName,
        key: String = this.key,
        value: TypeDescriptor = this.typeDescriptor
    ) = PseudoProperty(id, entityClassName, key, value)

    fun toDto() = PseudoPropertyDto(entitySimpleName, key, typeDescriptor)

    companion object {
        const val STORAGE_NAME = "pseudo_properties"
    }
}




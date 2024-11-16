package com.ecommercedemo.common.model

import com.ecommercedemo.common.application.JsonbConverter
import com.ecommercedemo.common.application.validation.classname.ValidEntityClassName
import com.ecommercedemo.common.model.dto.PseudoPropertyDto
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.util.*
import kotlin.reflect.KClass


@Suppress("unused")
@Entity
@Table(name = PseudoProperty.STORAGE_NAME)
open class PseudoProperty(
    override val id: UUID = UUID.randomUUID(),
    @ValidEntityClassName
    open val entityClassName: String = Any::class.qualifiedName!!,
    @NotNull
    open var key: String = "",
    @Convert(converter = JsonbConverter::class)
    @Column(columnDefinition = "jsonb")
    open var valueType: Any
) : BaseEntity() {
    constructor() : this(UUID.randomUUID(), Any::class.qualifiedName!!, "", "")

    private val entity: KClass<*>
        get() = Class.forName(entityClassName).kotlin

    val simpleEntityName: String
        get() = entity.simpleName ?: "Unknown"

    fun copy(
        id: UUID = this.id,
        entityClassName: String = this.entityClassName,
        key: String = this.key,
        value: Any = this.valueType
    ) = PseudoProperty(id, entityClassName, key, value)

    fun toDto() = PseudoPropertyDto(entityClassName, key, valueType)

    companion object {
        const val STORAGE_NAME = "pseudo_properties"
    }
}




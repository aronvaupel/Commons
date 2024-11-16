package com.ecommercedemo.common.model

import com.ecommercedemo.common.util.JsonbConverter
import com.ecommercedemo.common.validation.classname.ValidEntityClassName
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.util.*
import kotlin.reflect.KClass


@Suppress("unused")
open class PseudoProperty(
    @Id
    @GeneratedValue(generator = "uuid")
    open val id: UUID = UUID.randomUUID(),
    @ValidEntityClassName
    val entityClassName: String = Any::class.qualifiedName!!,
    var key: String = "",
    @Convert(converter = JsonbConverter::class)
    @Column(columnDefinition = "jsonb")
    var valueType: Any? = null
) {
    private val entity: KClass<*>
        get() = Class.forName(entityClassName).kotlin

    val simpleEntityName: String
        get() = entity.simpleName ?: "Unknown"

    fun copy(
        id: UUID = this.id,
        entityClassName: String = this.entityClassName,
        key: String = this.key,
        value: Any? = this.valueType
    ) = PseudoProperty(id, entityClassName, key, value)
}




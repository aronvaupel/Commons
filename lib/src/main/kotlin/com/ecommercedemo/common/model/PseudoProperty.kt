package com.ecommercedemo.common.model

import com.ecommercedemo.common.util.JsonbConverter
import com.ecommercedemo.common.validation.classname.ValidEntityClassName
import jakarta.persistence.*
import java.util.*
import kotlin.reflect.KClass


@Suppress("unused")
@MappedSuperclass
abstract class PseudoProperty{
    @Id
    @GeneratedValue(generator = "uuid")
    open val id: UUID = UUID.randomUUID()
    @ValidEntityClassName
    open val entityClassName: String = Any::class.qualifiedName!!
    open var key: String = ""
    @Convert(converter = JsonbConverter::class)
    @Column(columnDefinition = "jsonb")
    open var value: String? = null

    private val entity: KClass<*>
        get() = Class.forName(entityClassName).kotlin

    val simpleEntityName: String
        get() = entity.simpleName ?: "Unknown"
}


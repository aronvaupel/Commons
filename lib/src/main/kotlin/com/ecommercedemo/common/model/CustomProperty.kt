package com.ecommercedemo.common.model

import com.ecommercedemo.common.util.jackson.JsonbConverter
import com.ecommercedemo.common.validation.classname.ValidEntityClassName
import jakarta.persistence.*
import java.util.*
import kotlin.reflect.KClass


@Suppress("unused")
@MappedSuperclass
abstract class CustomProperty{
    @Id
    @GeneratedValue(generator = "uuid")
    val id: UUID = UUID.randomUUID()
    @ValidEntityClassName
    val entityClassName: String = Any::class.qualifiedName!!
    var key: String = ""
    @Convert(converter = JsonbConverter::class)
    var value: String? = null

    private val entity: KClass<*>
        get() = Class.forName(entityClassName).kotlin

    val simpleEntityName: String
        get() = entity.simpleName ?: "Unknown"
}


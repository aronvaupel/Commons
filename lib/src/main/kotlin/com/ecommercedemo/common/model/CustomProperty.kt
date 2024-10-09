package com.ecommercedemo.common.model

import com.ecommercedemo.common.validation.classname.ValidEntityClassName
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.util.*
import kotlin.reflect.KClass

@Suppress("unused")
@MappedSuperclass
abstract class CustomProperty<V : Any>(
    @Id
    @GeneratedValue(generator = "uuid")
    val id: UUID,
    @ValidEntityClassName
    val entityClassName: String,
    var key: String,
    var value: V
) {
    private val entity: KClass<*>
        get() = Class.forName(entityClassName).kotlin

    val simpleEntityName: String
        get() = entity.simpleName ?: "Unknown"
}
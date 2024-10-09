package com.ecommercedemo.common.model

import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.util.*
import kotlin.reflect.KClass

@Suppress("unused")
@MappedSuperclass
abstract class CustomProperty<V: Any> (
    @Id
    @GeneratedValue(generator = "uuid")
    val id: UUID,
    private val entityClassName: String,
    var key: String,
    var value: V
    ) {
    val entity: KClass<*>
        get() = Class.forName(entityClassName).kotlin
}
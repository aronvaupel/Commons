package com.ecommercedemo.common.model

import com.ecommercedemo.common.validation.classname.ValidEntityClassName
import jakarta.persistence.*
import java.util.*
import kotlin.reflect.KClass

@Suppress("unused")
@MappedSuperclass
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["entityClassName", "key"])]
)
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
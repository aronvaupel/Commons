package com.ecommercedemo.common.model

import com.ecommercedemo.common.util.JsonbConverter
import com.ecommercedemo.common.util.JsonbUserType
import com.ecommercedemo.common.validation.classname.ValidEntityClassName
import jakarta.persistence.*
import org.hibernate.annotations.Type
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
    @Convert(converter = JsonbConverter::class)
    @Type(value = JsonbUserType::class)
    var value: V
) {
    private val entity: KClass<*>
        get() = Class.forName(entityClassName).kotlin

    val simpleEntityName: String
        get() = entity.simpleName ?: "Unknown"
}
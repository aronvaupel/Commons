package com.ecommercedemo.common.model

import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.util.*

@Suppress("unused")
@MappedSuperclass
abstract class CustomProperty<E: Any, V: Any> (
    @Id
    @GeneratedValue(generator = "uuid")
    val id: UUID,
    val entity: E,
    val key: String,
    val value: V
    )
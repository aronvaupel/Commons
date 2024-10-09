package com.ecommercedemo.model

@Suppress("unused")
open class CustomProperty<E: Any, V: Any> (
    val entity: E,
    val key: String,
    val value: V
    )
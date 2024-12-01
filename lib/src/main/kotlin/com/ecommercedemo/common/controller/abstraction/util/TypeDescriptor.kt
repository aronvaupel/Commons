package com.ecommercedemo.common.controller.abstraction.util

import com.ecommercedemo.common.application.validation.type.ValueType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@Suppress("unused")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "category")
@JsonSubTypes(
    JsonSubTypes.Type(value = PrimitiveDescriptor::class, name = "PRIMITIVE"),
    JsonSubTypes.Type(value = TimeDescriptor::class, name = "TIME"),
    JsonSubTypes.Type(value = CollectionDescriptor::class, name = "COLLECTION"),
    JsonSubTypes.Type(value = MapDescriptor::class, name = "MAP"),
    JsonSubTypes.Type(value = ComplexObjectDescriptor::class, name = "COMPLEX")
)
interface TypeDescriptor {
    val type: ValueType
}


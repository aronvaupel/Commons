package com.ecommercedemo.common.application.search

import com.ecommercedemo.common.application.validation.type.ValueType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@Suppress("unused")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "category")
@JsonSubTypes(
    JsonSubTypes.Type(value = TypeDescriptor.PrimitiveDescriptor::class, name = "PRIMITIVE"),
    JsonSubTypes.Type(value = TypeDescriptor.TimeDescriptor::class, name = "TIME"),
    JsonSubTypes.Type(value = TypeDescriptor.CollectionDescriptor::class, name = "COLLECTION"),
    JsonSubTypes.Type(value = TypeDescriptor.ComplexObjectDescriptor::class, name = "COMPLEX")
)
sealed class TypeDescriptor {

    data class PrimitiveDescriptor(
        val type: ValueType
    ) : TypeDescriptor()

    data class CollectionDescriptor(
        val collectionType: ValueType,
        val itemDescriptor: TypeDescriptor
    ) : TypeDescriptor()

    data class TimeDescriptor(
        val timeType: ValueType
    ) : TypeDescriptor()

    data class ComplexObjectDescriptor(
        val fields: Map<String, TypeDescriptor>
    ) : TypeDescriptor()
}


package com.ecommercedemo.common.controller.abstraction.util


import com.ecommercedemo.common.application.validation.type.TypeCategory
import com.ecommercedemo.common.application.validation.type.ValueType
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "category"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TypeDescriptor.PrimitiveDescriptor::class, name = "PRIMITIVE"),
    JsonSubTypes.Type(value = TypeDescriptor.TimeDescriptor::class, name = "TIME"),
    JsonSubTypes.Type(value = TypeDescriptor.CollectionDescriptor::class, name = "COLLECTION"),
    JsonSubTypes.Type(value = TypeDescriptor.MapDescriptor::class, name = "MAP"),
    JsonSubTypes.Type(value = TypeDescriptor.ComplexObjectDescriptor::class, name = "COMPLEX")
)
sealed class TypeDescriptor {
    @get:JsonIgnore
    abstract val category: String
    abstract val type: ValueType

    data class PrimitiveDescriptor(
        override val category: String = TypeCategory.PRIMITIVE.name,
        override val type: ValueType,
        val isNullable: Boolean
    ) : TypeDescriptor()

    data class TimeDescriptor(
        override val category: String = TypeCategory.TIME.name,
        override val type: ValueType,
        val isNullable: Boolean
    ) : TypeDescriptor()

    data class CollectionDescriptor(
        override val category: String = TypeCategory.COLLECTION.name,
        override val type: ValueType,
        val itemDescriptor: TypeDescriptor,
        val minElements: Int,
        val maxElements: Int?
    ) : TypeDescriptor()

    data class MapDescriptor(
        override val category: String = TypeCategory.MAP.name,
        override val type: ValueType,
        val keyDescriptor: TypeDescriptor,
        val valueDescriptor: TypeDescriptor,
        val minEntries: Int,
        val maxEntries: Int?
    ) : TypeDescriptor()

    data class ComplexObjectDescriptor(
        override val category: String = TypeCategory.COMPLEX.name,
        override val type: ValueType,
        val isNullable: Boolean,
        val fields: Map<String, TypeDescriptor>
    ) : TypeDescriptor()
}


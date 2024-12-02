package com.ecommercedemo.common.controller.abstraction.util


import com.ecommercedemo.common.application.validation.type.TypeCategory
import com.ecommercedemo.common.application.validation.type.ValueType
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "category" // Use the "category" field for type discrimination
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TypeDescriptor.PrimitiveDescriptor::class, name = "PRIMITIVE"),
    JsonSubTypes.Type(value = TypeDescriptor.TimeDescriptor::class, name = "TIME"),
    JsonSubTypes.Type(value = TypeDescriptor.CollectionDescriptor::class, name = "COLLECTION"),
    JsonSubTypes.Type(value = TypeDescriptor.MapDescriptor::class, name = "MAP"),
    JsonSubTypes.Type(value = TypeDescriptor.ComplexObjectDescriptor::class, name = "COMPLEX")
)
sealed class TypeDescriptor {
    abstract val category: TypeCategory
    abstract val type: ValueType

    @JsonTypeName("PRIMITIVE")
    data class PrimitiveDescriptor(
        override val category: TypeCategory = TypeCategory.PRIMITIVE,
        override val type: ValueType,
        val isNullable: Boolean
    ) : TypeDescriptor()

    @JsonTypeName("TIME")
    data class TimeDescriptor(
        override val category: TypeCategory = TypeCategory.TIME,
        override val type: ValueType,
        val isNullable: Boolean
    ) : TypeDescriptor()

    @JsonTypeName("COLLECTION")
    data class CollectionDescriptor(
        override val category: TypeCategory = TypeCategory.COLLECTION,
        override val type: ValueType,
        val itemDescriptor: TypeDescriptor,
        val minElements: Int,
        val maxElements: Int?
    ) : TypeDescriptor()

    @JsonTypeName("MAP")
    data class MapDescriptor(
        override val category: TypeCategory = TypeCategory.MAP,
        override val type: ValueType,
        val keyDescriptor: TypeDescriptor,
        val valueDescriptor: TypeDescriptor,
        val minEntries: Int,
        val maxEntries: Int?
    ) : TypeDescriptor()

    @JsonTypeName("COMPLEX")
    data class ComplexObjectDescriptor(
        override val category: TypeCategory = TypeCategory.COMPLEX,
        override val type: ValueType,
        val isNullable: Boolean,
        val fields: Map<String, TypeDescriptor>
    ) : TypeDescriptor()
}

